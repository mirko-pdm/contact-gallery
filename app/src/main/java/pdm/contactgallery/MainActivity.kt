package pdm.contactgallery

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pdm.contactgallery.galleriesList.GalleriesListActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Splash screen, then check permissions
        Handler(mainLooper).postDelayed({
            reqPermissions.launch(arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }, 1500)
    }

    private val reqPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if(result.map { it.value }.contains(false)) {
            // Some permissions are missing, prompt user to grant all permissions
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Permissions are ok, go to the galleries activity
            val intent = Intent(this, GalleriesListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}