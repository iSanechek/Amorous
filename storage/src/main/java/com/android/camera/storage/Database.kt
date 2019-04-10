package com.android.camera.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.camera.storage.dao.CandidateDao
import com.android.camera.storage.models.CandidateModel

@Database(entities = [(CandidateModel::class)], version = 8, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun candidateDao(): CandidateDao
}