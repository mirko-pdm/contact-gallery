package pdm.contactgallery.galleriesList

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_galleries_list.*
import pdm.contactgallery.R
import pdm.contactgallery.database.DBHelper

class GalleriesListActivity : AppCompatActivity() {
    /*
    * Pick a contact from system's contacts app and create a new gallery with its name
    * */
    private val getContact = registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        uri?.also { uri ->
            val cursor = contentResolver.query(uri, null, null, null, null)
            val contactName = cursor?.let { c ->
                c.moveToFirst()
                c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
            }

            contactName?.also {
                DBHelper(this).createGallery(uri, it)
                refreshList()
            } ?: Toast.makeText(baseContext, getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galleries_list)
        supportActionBar?.title = resources.getString(R.string.myGalleries)

        fabAdd?.also {
            it.setOnClickListener { getContact.launch(null) }
        }

       refreshList()
    }

    private fun refreshList() {
        val galleries = DBHelper(this).listGalleries()

        supportFragmentManager.commit {
            setReorderingAllowed(true)

            val nextFragment =
                if (galleries.size <= 0)
                    Fragment(R.layout.fragment_no_galleries)
                else
                    GalleriesListFragment(galleries, ::refreshList)

            replace(R.id.galleriesListFragmentContainer, nextFragment)
        }
    }
}