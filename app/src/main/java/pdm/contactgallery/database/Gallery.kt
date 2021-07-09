package pdm.contactgallery.database

import android.net.Uri

data class Gallery(
    val id: Long,
    val uri: Uri,
    val name: String
)
