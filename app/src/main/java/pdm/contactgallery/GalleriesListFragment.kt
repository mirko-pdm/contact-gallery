package pdm.contactgallery

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_galleries_list.*
import pdm.contactgallery.database.Gallery

class GalleriesListFragment(private val galleries: MutableList<Gallery>) :
    Fragment(R.layout.fragment_galleries_list),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    constructor() : this(mutableListOf())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleriesList.adapter = GalleriesAdapter(requireContext(), galleries)
        galleriesList.onItemClickListener = this
        galleriesList.onItemLongClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.i("bbb",  galleries[position].name)
        Toast.makeText(activity, galleries[position].name, Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ): Boolean {
        Log.i("bbb",  "Long " + galleries[position].name)
        Toast.makeText(activity, "Long " + galleries[position].name, Toast.LENGTH_SHORT).show()
        return true
    }
}