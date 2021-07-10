package pdm.contactgallery.galleriesList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_galleries_list.*
import pdm.contactgallery.gallery.GalleryActivity
import pdm.contactgallery.R
import pdm.contactgallery.database.DBHelper
import pdm.contactgallery.database.Gallery
import java.io.FileFilter

class GalleriesListFragment(
    private val galleries: MutableList<Gallery>,
    private val refreshCallback: () -> Unit
) :
    Fragment(R.layout.fragment_galleries_list),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    constructor() : this(mutableListOf(), {})

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleriesList.adapter = GalleriesListAdapter(requireContext(), galleries)
        galleriesList.onItemClickListener = this
        galleriesList.onItemLongClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Open gallery
        startActivity(
            Intent(requireContext(), GalleryActivity::class.java)
                .putExtra("galleryId", galleries[position].id)
                .putExtra("galleryName", galleries[position].name)
        )
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        // Open gallery options
        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayOf(getString(R.string.deleteGalleryPrompt, galleries[position].name))) { _, which ->
                when(which) {
                    // Ask for confirmation
                    0 -> MaterialAlertDialogBuilder(requireContext(),
                        R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
                    )
                            .setTitle(R.string.deleteGallery)
                            .setMessage(R.string.deleteGalleryConfirm)
                            .setNeutralButton(R.string.cancel, null)
                            .setPositiveButton(R.string.yesDelete) { _, _ ->
                                DBHelper(requireContext()).deleteGallery(id)

                                val fileFilter = FileFilter { file ->
                                    file.name.startsWith("${id}_")
                                }

                                val galleryFiles =
                                    context?.getExternalFilesDir(GalleryActivity.GALLERY_DIR)?.listFiles(fileFilter)
                                        ?: arrayOf()
                                val thumbnailFile =
                                    context?.getExternalFilesDir(GalleryActivity.THUMBNAILS_DIR)?.listFiles(fileFilter)
                                        ?: arrayOf()

                                (galleryFiles + thumbnailFile).forEach { file ->
                                    if(file.exists() and file.isFile) file.delete()
                                }
                                refreshCallback()
                            }
                            .show()
                }
            }
            .show()

        return true
    }
}