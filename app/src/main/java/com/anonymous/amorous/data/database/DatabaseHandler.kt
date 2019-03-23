package com.anonymous.amorous.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.Event
import com.anonymous.amorous.debug.logDebug

interface LocalDatabase {
    fun saveCandidates(items: List<Candidate>)
    fun saveCandidate(candidate: Candidate)
    fun updateCandidate(item: Candidate)
    fun getCandidates(): List<Candidate>
    fun getCandidate(id: Int): Candidate
    fun removeCandidate(candidate: Candidate)
    fun clearDb()
    fun getCandidates(select: String, args: Array<String>?): List<Candidate>

    fun saveEvent(event: Event)
    fun getEvent(id: String): Event
    fun updateEvent(event: Event)
    fun clearEvents()
}

class DatabaseHandler(context: Context) : LocalDatabase, SQLiteOpenHelper(context, "candidate.db", null, 4) {


    override fun saveCandidate(candidate: Candidate) {
        val db = this@DatabaseHandler.writableDatabase
        db.transaction {
            insert(Candidate.TABLE_NAME, null, getCvFromCandidate(candidate))
        }
    }

    override fun saveEvent(event: Event) {
        val db = this@DatabaseHandler.writableDatabase
        db.transaction {
            insert(Event.TABLE_NAME, null, getCvFromEvent(event))
        }
    }

    override fun getEvent(id: String): Event {
        val db = this@DatabaseHandler.writableDatabase
        val select = "SELECT * FROM ${Event.TABLE_NAME} WHERE ${Event.COLUMN_ID} = $id"
        val c = db.rawQuery(select, null)
        c?.moveToFirst()
        val event = getEventItem(c)
        c.close()
        return event
    }

    override fun updateEvent(event: Event) {
        val db = this@DatabaseHandler.writableDatabase
        val v = getCvFromEvent(event, true)
        db.transaction {
            update(Event.TABLE_NAME, v, "${Event.COLUMN_ID} = ${event.id}", null)
        }
    }

    override fun clearEvents() {
        val db = this@DatabaseHandler.writableDatabase
        db.transaction {
            delete(Event.TABLE_NAME, null, null)
        }
    }

    override fun saveCandidates(items: List<Candidate>) {
        val count = getCount()
        val cacheItems = getCandidates()
        val db = this@DatabaseHandler.writableDatabase
        when (count) {
            0 -> {
                logDebug {
                    "Db empty! Insert data!"
                }
                for (item in items) {
                    db.transaction {
                        insert(Candidate.TABLE_NAME, null, getCvFromCandidate(item))
                    }
                }
            }
            else -> {
                logDebug {
                    "Db is not empty! Size $count"
                }


                val cacheIds = cacheItems.map { it.uid }.toSet()
                for (item in items) {
                    when {
                        item.uid in cacheIds -> updateCandidate(item)
                        else -> db.transaction {
                            insert(Candidate.TABLE_NAME, null, getCvFromCandidate(item))
                        }
                    }
                }
            }
        }
    }

    override fun updateCandidate(item: Candidate) {
        val db = this@DatabaseHandler.writableDatabase
        val v = getCvFromCandidate(item, true)
        db.transaction {
            update(Candidate.TABLE_NAME, v, "${Candidate.COLUMN_UID} = ${item.uid}", null)
        }
    }

    override fun getCandidates(): List<Candidate> = getCandidates("SELECT * FROM ${Candidate.TABLE_NAME}", null)

    override fun getCandidates(select: String, args: Array<String>?): List<Candidate> {
        val temp = mutableListOf<Candidate>()
        val db = this@DatabaseHandler.writableDatabase
        val c = db.rawQuery(select, args)
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    temp.add(getCandidateItem(c))
                } while (c.moveToNext())
            }
        }
        c.close()
        return temp
    }

    override fun getCandidate(id: Int): Candidate {
        val db = this@DatabaseHandler.writableDatabase
        val s = "SELECT * FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_UID} = $id"
        val c = db.rawQuery(s, null)
        c?.moveToFirst()
        val item = getCandidateItem(c)
        c.close()
        return item
    }

    override fun removeCandidate(candidate: Candidate) {
        val id = candidate.uid
        id ?: return
        val db = this@DatabaseHandler.writableDatabase
//        val s = "DELETE FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_UID} = $id"
//        val c = db.rawQuery(s, null)
//        c?.moveToFirst()

        Log.d("Boom", "Boom11")
        db.transaction {
            delete(Candidate.TABLE_NAME, "${Candidate.COLUMN_UID} =?", arrayOf("$id"))
        }
    }

    override fun clearDb() {
        val db = this@DatabaseHandler.writableDatabase
        db.transaction {
            delete(Candidate.TABLE_NAME, null, null)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE_CANDIDATE = "CREATE TABLE ${Candidate.TABLE_NAME} (" +
                "${Candidate.COLUMN_UID} $DB_COLUMN_INTEGER, " +
                "${Candidate.COLUMN_NAME} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_THUMBNAIL_STATUS} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_TEMP_PATH} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_ORIGINAL_PATH} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_SIZE} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_ORIGINAL_STATUS} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_TYPE} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_BACKUP_STATUS} $DB_COLUMN_TEXT, " +
                "${Candidate.COLUMN_DATE} $DB_COLUMN_INTEGER);"

        val CREATE_TABLE_EVENT = "CREATE TABLE ${Event.TABLE_NAME} (" +
                "${Event.COLUMN_ID} $DB_COLUMN_TEXT, " +
                "${Event.COLUMN_TITLE} $DB_COLUMN_TEXT, " +
                "${Event.COLUMN_DATE} $DB_COLUMN_INTEGER, " +
                "${Event.COLUMN_EVENT} $DB_COLUMN_TEXT);"

        db?.execSQL(CREATE_TABLE_CANDIDATE)
        db?.execSQL(CREATE_TABLE_EVENT)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE_CANDIDATES = "DROP TABLE IF EXISTS ${Candidate.TABLE_NAME}"
        val DROP_TABLE_EVENT = "DROP TABLE IF EXISTS ${Event.TABLE_NAME}"
        db?.execSQL(DROP_TABLE_CANDIDATES)
        db?.execSQL(DROP_TABLE_EVENT)
        onCreate(db)
    }

    private fun getEventItem(c: Cursor): Event = Event(
            id = c.getString(c.getColumnIndex(Event.COLUMN_ID)),
            title = c.getString(c.getColumnIndex(Event.COLUMN_TITLE)),
            date = c.getLong(c.getColumnIndex(Event.COLUMN_DATE)),
            event = c.getString(c.getColumnIndex(Event.COLUMN_EVENT))
    )

    private fun getCvFromEvent(item: Event, update: Boolean = false): ContentValues {
        val v = ContentValues()
        with(v) {
            if (!update) put(Event.COLUMN_ID, item.id)
            put(Event.COLUMN_TITLE, item.title)
            put(Event.COLUMN_DATE, item.date)
            put(Event.COLUMN_EVENT, item.event)
        }
        return v
    }

    /*Candidates*/
    private fun getCandidateItem(c: Cursor) : Candidate = Candidate(
            uid = c.getInt(c.getColumnIndex(Candidate.COLUMN_UID)),
            name = c.getString(c.getColumnIndex(Candidate.COLUMN_NAME)),
            backupStatus = c.getString(c.getColumnIndex(Candidate.COLUMN_BACKUP_STATUS)),
            originalStatus = c.getString(c.getColumnIndex(Candidate.COLUMN_ORIGINAL_STATUS)),
            originalPath = c.getString(c.getColumnIndex(Candidate.COLUMN_ORIGINAL_PATH)),
            tempPath = c.getString(c.getColumnIndex(Candidate.COLUMN_TEMP_PATH)),
            thumbnailStatus = c.getString(c.getColumnIndex(Candidate.COLUMN_THUMBNAIL_STATUS)),
            size = c.getString(c.getColumnIndex(Candidate.COLUMN_SIZE)),
            type = c.getString(c.getColumnIndex(Candidate.COLUMN_TYPE)),
            date = c.getLong(c.getColumnIndex(Candidate.COLUMN_DATE))
    )

    private fun getCvFromCandidate(item: Candidate, update: Boolean = false): ContentValues {
        val v = ContentValues()
        with(v) {
            if (!update) put(Candidate.COLUMN_UID, item.uid)
            put(Candidate.COLUMN_NAME, item.name)
            put(Candidate.COLUMN_THUMBNAIL_STATUS, item.thumbnailStatus)
            put(Candidate.COLUMN_TEMP_PATH, item.tempPath)
            put(Candidate.COLUMN_ORIGINAL_PATH, item.originalPath)
            put(Candidate.COLUMN_SIZE, item.size)
            put(Candidate.COLUMN_ORIGINAL_STATUS, item.originalStatus)
            put(Candidate.COLUMN_TYPE, item.type)
            put(Candidate.COLUMN_BACKUP_STATUS, item.backupStatus)
            put(Candidate.COLUMN_DATE, item.date)
        }
        return v
    }

    private fun removeCandidate(id: Int) {
        val db = this@DatabaseHandler.writableDatabase
//        val s = "DELETE FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_UID} =:$id"
//        val c = db.rawQuery(s, null)
//        c.moveToFirst()
//        c.close()

        db.transaction {
            delete(Candidate.TABLE_NAME, "${Candidate.COLUMN_UID} =?", arrayOf("$id"))
        }

        db.close()
    }

    private fun getCount(): Int {
        val db = this@DatabaseHandler.readableDatabase
        val s = "SELECT count(*) FROM ${Candidate.TABLE_NAME}"
        val c = db.rawQuery(s, null)
        c.moveToFirst()
        val count = c.getInt(0)
        c.close()
        db.close()
        return count
    }

    companion object {
        private const val DB_COLUMN_INTEGER = "INTEGER"
        private const val DB_COLUMN_TEXT = "TEXT"
    }
}