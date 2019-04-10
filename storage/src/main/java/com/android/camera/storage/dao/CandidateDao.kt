package com.android.camera.storage.dao

import androidx.room.*
import com.android.camera.storage.models.CandidateModel

@Dao
interface CandidateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<CandidateModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(candidate: CandidateModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(candidate: CandidateModel)

    @Delete
    fun delete(candidate: CandidateModel)

    @Query("DELETE FROM candidates_table WHERE uid =:uid")
    fun delete(uid: String)

    @Query("DELETE FROM candidates_table")
    fun deleteAll()

    @Query("SELECT count(*) FROM candidates_table")
    fun getSize(): Int

    @Query("SELECT * FROM candidates_table")
    fun getAll(): List<CandidateModel>

    @Query("SELECT * FROM candidates_table WHERE uid =:uid")
    fun getCandidate(uid: String): CandidateModel

    @Query("SELECT * FROM candidates_table WHERE thumbnail_status =:queryParameter ORDER BY date ASC LIMIT :limit")
    fun getThumbnails(queryParameter: String, limit: Int): List<CandidateModel>

    @Query("SELECT * FROM candidates_table WHERE original_status =:queryParameter ORDER BY date ASC LIMIT :limit")
    fun getOriginals(queryParameter: String, limit: Int): List<CandidateModel>

    @Query("SELECT * FROM candidates_table WHERE backup_status =:queryParameter")
    fun getBackup(queryParameter: String): List<CandidateModel>

    @Query("UPDATE candidates_table SET thumbnail_status =:status WHERE uid =:uid")
    fun updateThumbnailsStatus(uid: String, status: String)

}