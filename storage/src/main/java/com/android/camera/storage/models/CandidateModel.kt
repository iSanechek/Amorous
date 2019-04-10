package com.android.camera.storage.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "candidates_table")
data class CandidateModel(@PrimaryKey val uid: String,
                          @ColumnInfo(name = "remote_uid") val remoteUid: String,
                          val name: String,
                          @ColumnInfo(name = "thumbnail_status") val thumbnailStatus: String,
                          @ColumnInfo(name = "temp_path") val tempPath: String,
                          @ColumnInfo(name = "original_path") val originalPath: String,
                          val size: Long,
                          @ColumnInfo(name = "original_status") val originalStatus: String,
                          val type: String,
                          @ColumnInfo(name = "backup_status") val backupStatus: String,
                          val date: Long)