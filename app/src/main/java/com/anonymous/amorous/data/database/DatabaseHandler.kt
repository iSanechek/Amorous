package com.anonymous.amorous.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.debug.logDebug

interface LocalDatabase {
    suspend fun saveCandidates(items: List<Candidate>)
    suspend fun updateCandidate(item: Candidate)
    suspend fun getCandidates(): List<Candidate>
    suspend fun getCandidate(id: Int): Candidate
    suspend fun clearDb()
    suspend fun getCandidates(select: String, args: Array<String>?): List<Candidate>
}

class DatabaseHandler(context: Context) : LocalDatabase, SQLiteOpenHelper(context, "candidate.db", null, 2) {

    override suspend fun saveCandidates(items: List<Candidate>) {
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
        db.close()
    }

    override suspend fun updateCandidate(item: Candidate) {
        val db = this@DatabaseHandler.writableDatabase
        val v = getCvFromCandidate(item, true)
        db.transaction {
            update(Candidate.TABLE_NAME, v, "${Candidate.COLUMN_UID} =:${item.uid}", null)
        }
        db.close()
    }

    override suspend fun getCandidates(): List<Candidate> = getCandidates("SELECT * FROM ${Candidate.TABLE_NAME}", null)

    override suspend fun getCandidates(select: String, args: Array<String>?): List<Candidate> {
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
        db.close()
        return temp
    }

    override suspend fun getCandidate(id: Int): Candidate {
        val db = this@DatabaseHandler.writableDatabase
        val s = "SELECT * FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_UID} =:$id"
        val c = db.rawQuery(s, null)
        c?.moveToFirst()
        val item = getCandidateItem(c)
        c.close()
        db.close()
        return item
    }

    override suspend fun clearDb() {
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
                "${Candidate.TABLE_NAME} $DB_COLUMN_INTEGER);"
        db?.execSQL(CREATE_TABLE_CANDIDATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE_PHOTOS = "DROP TABLE IF EXISTS ${Candidate.TABLE_NAME}"
        db?.execSQL(DROP_TABLE_PHOTOS)
        onCreate(db)
    }

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
        val s = "DELETE FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_UID} =:$id"
        val c = db.rawQuery(s, null)
        c.moveToFirst()
        c.close()
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