package com.example.agent_phone.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.agent_phone.model.Contact

class ContactsDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CONTACTS = "contacts"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ROLE = "role"
        const val COLUMN_PHONE_NUMBER = "phone_number"
        const val COLUMN_AVATAR_COLOR_INDEX = "avatar_color_index"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CONTACTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL,
                $COLUMN_PHONE_NUMBER TEXT NOT NULL,
                $COLUMN_AVATAR_COLOR_INDEX INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Insert initial default contact
        val initialContact = ContentValues().apply {
            put(COLUMN_ID, "dave_lawyer")
            put(COLUMN_NAME, "Dave")
            put(COLUMN_ROLE, "Lawyer")
            put(COLUMN_PHONE_NUMBER, "9392532052")
            put(COLUMN_AVATAR_COLOR_INDEX, 0)
        }
        db.insert(TABLE_CONTACTS, null, initialContact)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun getAllContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CONTACTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME ASC"
        )

        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
            val nameIndex = c.getColumnIndexOrThrow(COLUMN_NAME)
            val roleIndex = c.getColumnIndexOrThrow(COLUMN_ROLE)
            val phoneIndex = c.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER)
            val avatarIndex = c.getColumnIndexOrThrow(COLUMN_AVATAR_COLOR_INDEX)

            while (c.moveToNext()) {
                val id = c.getString(idIndex)
                val name = c.getString(nameIndex)
                val role = c.getString(roleIndex)
                val phoneNumber = c.getString(phoneIndex)
                val avatarColorIndex = c.getInt(avatarIndex)

                contactsList.add(
                    Contact(
                        id = id,
                        name = name,
                        role = role,
                        phoneNumber = phoneNumber,
                        avatarColorIndex = avatarColorIndex
                    )
                )
            }
        }
        return contactsList
    }

    fun insertContact(contact: Contact): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, contact.id)
            put(COLUMN_NAME, contact.name)
            put(COLUMN_ROLE, contact.role)
            put(COLUMN_PHONE_NUMBER, contact.phoneNumber)
            put(COLUMN_AVATAR_COLOR_INDEX, contact.avatarColorIndex)
        }
        val result = db.insertWithOnConflict(
            TABLE_CONTACTS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return result != -1L
    }

    fun deleteContact(id: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_CONTACTS, "$COLUMN_ID = ?", arrayOf(id))
        return result > 0
    }
}
