package pdm.contactgallery.gallery

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_gallery_grid.*
import pdm.contactgallery.R
import pdm.contactgallery.database.Gallery
import pdm.contactgallery.galleriesList.GalleriesListAdapter
import java.io.File

class GalleryFragment(
    private val fileList: MutableList<File>,
    private val refreshCallback: () -> Unit
) :
    Fragment(R.layout.fragment_gallery_grid),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    constructor() : this(mutableListOf(), {})

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryGrid.adapter = GalleryAdapter(requireContext(), fileList)
        galleryGrid.onItemClickListener = this
        galleryGrid.onItemLongClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val currentFile = fileList[position]
        val intent = when(currentFile.extension) {
            "jpg", "mp4" ->
                    Intent(Intent.ACTION_VIEW)
                        .setData(FileProvider.getUriForFile(requireContext(), "pdm.contactgallery.provider", currentFile))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            else -> null
        }

        startActivity(intent)
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        val currentFile = fileList[position]

        // Open gallery options
        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayOf(getString(R.string.deleteMediaPrompt, currentFile.name))) { _, which ->
                when(which) {
                    // Ask for confirmation
                    0 -> MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle(R.string.deleteMedia)
                            .setMessage(R.string.deleteMediaConfirm)
                            .setNeutralButton(R.string.cancel, null)
                            .setPositiveButton(R.string.yesDelete) { _, _ ->
                                val thumbnailFile = File(
                                    context?.getExternalFilesDir(GalleryActivity.THUMBNAILS_DIR),
                                    "${currentFile.nameWithoutExtension}.jpg")

                                if(currentFile.exists()) currentFile.delete()
                                if(thumbnailFile.exists()) thumbnailFile.delete()

                                refreshCallback()
                            }
                            .show()
                }
            }
            .show()

        return true
    }
}