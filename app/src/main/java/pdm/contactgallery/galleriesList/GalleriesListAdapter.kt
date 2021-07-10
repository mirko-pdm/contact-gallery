package pdm.contactgallery.galleriesList

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import pdm.contactgallery.R
import pdm.contactgallery.database.Gallery
import pdm.contactgallery.gallery.GalleryActivity
import java.io.FileFilter

class GalleriesListAdapter(private val context: Context, private val data: MutableList<Gallery>) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val newView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_galleries, parent, false)
        val cursor = context.contentResolver.query(data[position].uri, null, null, null, null)

        val thumbnailUri = cursor?.let { c: Cursor ->
            c.moveToFirst()
            c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
        }

//        val filterAudios = FileFilter { file ->
//            file.name.startsWith("${data[position].id}_") and
//                    (file.extension.lowercase() == "jpg")
//        }

        val location = context.getExternalFilesDir(GalleryActivity.GALLERY_DIR)

        val galleryNameView = newView.findViewById<TextView>(R.id.galleryName)
        val thumbnailView = newView.findViewById<ImageView>(R.id.thumbnail)
        val textPhotosView = newView.findViewById<TextView>(R.id.textPhotos)
        val textVideosView = newView.findViewById<TextView>(R.id.textVideos)
        val textAudiosView = newView.findViewById<TextView>(R.id.textVideos)

        galleryNameView.text = data[position].name

        textPhotosView.text = location?.listFiles(FileFilter { file ->
            file.name.startsWith("${data[position].id}_") and
                    (file.extension.lowercase() == "jpg")
        })?.size.toString() ?: "0"

        textVideosView.text = location?.listFiles(FileFilter { file ->
            file.name.startsWith("${data[position].id}_") and
                    (file.extension.lowercase() == "mp4")
        })?.size.toString() ?: "0"

        thumbnailUri?.also {
           thumbnailView .setImageURI(Uri.parse(it))
        }

        return newView
    }
}
