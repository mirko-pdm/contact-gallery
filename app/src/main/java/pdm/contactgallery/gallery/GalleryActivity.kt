package pdm.contactgallery.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_gallery.*
import pdm.contactgallery.R
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GalleryActivity : AppCompatActivity() {
    private var galleryId: Long = -1

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }
    private var addClicked = false

    private var currentFile: File? = null
    private var mediaRecorder = MediaRecorder()
    private var isRecording = false

    private val fileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // If activity result is OK create a thumbnail, otherwise delete the temporary file
        when(result.resultCode) {
            Activity.RESULT_OK -> {
                makeThumbnail(currentFile!!)
                refreshGrid()
            }
            Activity.RESULT_CANCELED -> {
                currentFile.also {
                    if(currentFile?.exists() == true) currentFile?.delete()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore temporary file
        savedInstanceState?.also {
            currentFile = it.getString("currentFile")?.let { file ->
                File(file)
            }
        }

        setContentView(R.layout.activity_gallery)
        galleryId = intent?.extras?.getLong("galleryId") ?: -1
        supportActionBar?.title =  intent?.extras?.getString("galleryName") ?: ""

        // Setup GUI buttons
        fabPhoto.setOnClickListener {
            fileResult.launch(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, makeNewFile("jpg"))
            )
        }

        fabVideo.setOnClickListener{
           fileResult.launch(
               Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                   .putExtra(MediaStore.EXTRA_OUTPUT, makeNewFile("mp4"))
           )
        }

        fabAudio.setOnClickListener{
            val recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

            if (recordPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                fabAddToGallery.hide()
                fabAudio.hide()
                fabPhoto.hide()
                fabVideo.hide()
                recordingOverlay.isClickable = true
                recordingOverlay.visibility = View.VISIBLE

                // Start audio recording
                makeNewFile("3gp")

                isRecording = true
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mediaRecorder.setOutputFile(currentFile?.absolutePath)
                mediaRecorder.prepare()
                mediaRecorder.start()
            }
        }

        fabAddToGallery.setOnClickListener{
            listOf(fabPhoto, fabAudio, fabVideo).forEach {
                if(!addClicked) {
                    it.visibility = View.VISIBLE
                    it.isClickable = true
                    it.startAnimation(fromBottom)
                } else {
                    it.visibility = View.INVISIBLE
                    it.isClickable = false
                    it.startAnimation(toBottom)
                }
            }

            fabAddToGallery.startAnimation(if(addClicked) rotateClose else rotateOpen)
            addClicked = !addClicked
        }

        recordingOverlay.setOnClickListener{
            recordingOverlay.isClickable = false
            recordingOverlay.visibility = View.INVISIBLE
            fabAddToGallery.show()
            fabAudio.show()
            fabPhoto.show()
            fabVideo.show()

            // Stop the recording
            mediaRecorder.stop()
            mediaRecorder.release()
            mediaRecorder = MediaRecorder()
            isRecording = false

            refreshGrid()
        }

        refreshGrid()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("currentFile", currentFile?.absolutePath)
    }

    override fun onPause() {
        super.onPause()

        // Stop the recording when pausing activity
        if(isRecording) {
            mediaRecorder.stop()
            mediaRecorder.release()
            mediaRecorder = MediaRecorder()
        }
    }

    // Generates a new temporary file
    private fun makeNewFile(extension: String): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        currentFile = File.createTempFile(
            "${galleryId}_${timeStamp}_",
            ".$extension",
            getExternalFilesDir(GALLERY_DIR))

        return FileProvider.getUriForFile(this, "pdm.contactgallery.provider", currentFile!!)
    }

    // Creates a thumbnail for pictures and video files
    private fun makeThumbnail(file: File) {
        val size = resources.getDimension(R.dimen.thumbnail_size).toInt()

        val thumbnail = when(file.extension) {
            "jpg" -> ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(file.absolutePath), size, size)
            "mp4" -> ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Images.Thumbnails.MINI_KIND)
            else -> null
        }

        thumbnail?.also {
            val thumbnailFile = File(
                getExternalFilesDir(THUMBNAILS_DIR),
                "${file.nameWithoutExtension}.jpg")

            it.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                FileOutputStream(thumbnailFile))
        }
    }

    private fun refreshGrid() {
        // Find all files in this gallery
        val fileFilter = FileFilter { file ->
            file.name.startsWith("${galleryId}_")
        }

        val fileList = getExternalFilesDir(GALLERY_DIR)?.listFiles(fileFilter) ?: arrayOf<File>()
        fileList.sortWith { a, b ->
            when {
                a.name > b.name -> -1
                a.name < b.name -> 1
                else -> 0
            }
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)

            val nextFragment =
                if (fileList.isEmpty())
                    Fragment(R.layout.fragment_gallery_empty)
                else
                    GalleryFragment(fileList.toMutableList(), ::refreshGrid)

            replace(R.id.galleryFragmentContainer, nextFragment)
        }
    }

    companion object {
        const val GALLERY_DIR = "Galleries"
        const val THUMBNAILS_DIR = "Thumbnails"
    }
}