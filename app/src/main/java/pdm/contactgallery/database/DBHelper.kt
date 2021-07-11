package pdm.contactgallery.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
                CREATE TABLE $GALLERIES_TABLE (
                    ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_URI TEXT NOT NULL UNIQUE,
                    $COL_NAME TEXT NOT NULL
                )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // At the moment there's only version 1
    }

    // Helper for creating a new gallery
    fun createGallery(uri: Uri, name: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_URI, uri.toString())
            put(COL_NAME, name)
        }

        return db?.insert(GALLERIES_TABLE, null, values) ?: -1
    }

    // Helper for deleting a gallery
    fun deleteGallery(id: Long) {
        writableDatabase.delete(
            GALLERIES_TABLE,
            "${BaseColumns._ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun updateGalleryName(id: Long, name: String) {
        val values = ContentValues().apply {
            put(COL_NAME, name)
        }

        writableDatabase.update(
           GALLERIES_TABLE,
           values,
            "${BaseColumns._ID} = ?",
            arrayOf(id.toString())
        )
    }

    // Helper for listing galleries
    fun listGalleries(): MutableList<Gallery> {
        val db = readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            COL_URI,
            COL_NAME
        )

        val cursor = db.query(
            GALLERIES_TABLE,
            projection,
            null,
            null,
            null,
            null,
            "$COL_NAME ASC"
        )

        val list = mutableListOf<Gallery>()
        with(cursor) {
            while(moveToNext()) {
                list.add(
                    Gallery(
                        getLong(getColumnIndex(BaseColumns._ID)),
                        Uri.parse(getString(getColumnIndex(COL_URI))),
                        getString(getColumnIndex(COL_NAME))
                    ))
            }
        }

        return list
    }

    companion object {
        const val DATABASE_NAME = "contactGallery.db"
        const val DATABASE_VERSION = 1

        const val GALLERIES_TABLE = "Galleries"
        const val COL_URI = "URI"
        const val COL_NAME = "Name"
    }
}