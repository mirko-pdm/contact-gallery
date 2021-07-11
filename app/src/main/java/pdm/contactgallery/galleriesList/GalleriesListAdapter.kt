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
import pdm.contactgallery.database.DBHelper
import pdm.contactgallery.database.Gallery
import pdm.contactgallery.gallery.GalleryActivity

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
        val thisGallery = data[position]
        val newView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_galleries, parent, false)
        val cursor = context.contentResolver.query(data[position].uri, null, null, null, null)
        var deletedContact = false
        var thumbnailUri: String? = null
        var currentName: String? = null

        cursor?.let { c: Cursor ->
            if(c.moveToFirst()) {
                thumbnailUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                currentName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
            } else {
                deletedContact = true
            }
        }

        // If name has changed update it on the database
        currentName?.also {
            if (it != thisGallery.name) DBHelper(context).updateGalleryName(thisGallery.id, it)
        }

        val location = context.getExternalFilesDir(GalleryActivity.GALLERY_DIR)

        val galleryNameView = newView.findViewById<TextView>(R.id.galleryName)
        val thumbnailView = newView.findViewById<ImageView>(R.id.thumbnail)
        val textPhotosView = newView.findViewById<TextView>(R.id.textPhotos)
        val textVideosView = newView.findViewById<TextView>(R.id.textVideos)
        val textAudiosView = newView.findViewById<TextView>(R.id.textAudios)

        galleryNameView.text = thisGallery.name +
                    (if(deletedContact) (" [" + context.resources.getString(R.string.deleted) + "]") else "")


        // Getting number of each kind of media in the gallery
        textPhotosView.text = location?.listFiles { file ->
            file.name.startsWith("${thisGallery.id}_") and
                    (file.extension.lowercase() == "jpg")
        }?.size.toString()

        textVideosView.text = location?.listFiles { file ->
            file.name.startsWith("${thisGallery.id}_") and
                    (file.extension.lowercase() == "mp4")
        }?.size.toString()

        textAudiosView.text = location?.listFiles { file ->
            file.name.startsWith("${thisGallery.id}_") and
                    (file.extension.lowercase() == "3gp")
        }?.size.toString()

        thumbnailUri?.also {
           thumbnailView .setImageURI(Uri.parse(it))
        }

        return newView
    }
}
