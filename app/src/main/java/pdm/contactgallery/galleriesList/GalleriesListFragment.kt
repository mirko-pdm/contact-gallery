package pdm.contactgallery.galleriesList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_galleries_list.*
import pdm.contactgallery.R
import pdm.contactgallery.database.DBHelper
import pdm.contactgallery.database.Gallery
import pdm.contactgallery.gallery.GalleryActivity
import java.io.FileFilter

class GalleriesListFragment(
    private val galleries: MutableList<Gallery>,
    private val refreshCallback: () -> Unit
) :
    Fragment(R.layout.fragment_galleries_list),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    private val startThenRefresh = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshCallback()
    }

    constructor() : this(mutableListOf(), {})

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleriesList.adapter = GalleriesListAdapter(requireContext(), galleries)
        galleriesList.onItemClickListener = this
        galleriesList.onItemLongClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Open gallery and refresh grid when returning
       startThenRefresh.launch(Intent(requireContext(), GalleryActivity::class.java)
            .putExtra("galleryId", galleries[position].id)
            .putExtra("galleryName", galleries[position].name))
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        // Open gallery options  (delete)
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

                                // Delete all files and thumbnails for this particular gallery
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