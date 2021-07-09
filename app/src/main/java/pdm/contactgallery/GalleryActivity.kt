package pdm.contactgallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File

class GalleryActivity : AppCompatActivity() {
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }
    private var addClicked = false

    private lateinit var currentFile: File

    private val fileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when(result.resultCode) {
            Activity.RESULT_OK -> {}
            Activity.RESULT_CANCELED -> {
                currentFile.also {
                    if(currentFile.exists()) currentFile.delete()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        fabPhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, makeNewFile("jpg"))
            fileResult.launch(intent)
        }

        fabVideo.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, makeNewFile("mp4"))
            fileResult.launch(intent)
        }

        fabAudio.setOnClickListener{
            Toast.makeText(this, "audio", Toast.LENGTH_SHORT).show()
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
    }

    private fun makeNewFile(extension: String): Uri {
        currentFile = File.createTempFile(
            "IMG_",
            ".$extension",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        return FileProvider.getUriForFile(this, "pdm.contactgallery.provider", currentFile)
    }
}