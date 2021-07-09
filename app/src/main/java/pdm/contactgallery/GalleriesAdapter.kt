package pdm.contactgallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import pdm.contactgallery.database.Gallery

class GalleriesAdapter(private val context: Context, private val data: MutableList<Gallery>) : BaseAdapter() {
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

        val bb = newView.findViewById<TextView>(R.id.galleryName)
        bb.text = data[position].name
        return newView
    }
}
