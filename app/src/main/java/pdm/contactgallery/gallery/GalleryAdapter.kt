package pdm.contactgallery.gallery

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import pdm.contactgallery.R
import java.io.File

class GalleryAdapter(private val context: Context, private val data: MutableList<File>) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentFile = data[position]
        val thumbnailFile = File(
            context.getExternalFilesDir(GalleryActivity.THUMBNAILS_DIR),
            "${currentFile.nameWithoutExtension}.jpg"
        )

        val view = when(currentFile.extension) {
            "jpg" -> R.layout.item_gallery_photo
            "mp4" -> R.layout.item_gallery_video
            "3gp" -> R.layout.item_gallery_audio
            else -> R.layout.item_gallery_unknown
        }

        //  We can't recycle the view as we have different layouts for each kind of media
        val newView = LayoutInflater.from(context).inflate(view, parent, false)

        // Rotate thumbnails based on image rotation
        BitmapFactory.decodeFile(thumbnailFile.absolutePath)?.also {
            newView.findViewById<ImageView>(R.id.thumbnail).setImageBitmap(it)

            val orientation = ExifInterface(currentFile.absolutePath)
                .getAttribute(ExifInterface.TAG_ORIENTATION)?.toIntOrNull()

            newView.findViewById<ImageView>(R.id.thumbnail).rotation = when(orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> 90f
                else -> 0f
            }
        }
        return newView
    }
}
