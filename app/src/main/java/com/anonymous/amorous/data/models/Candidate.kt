package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//
@IgnoreExtraProperties
data class Candidate(var uid: Int = 0,
                     var remoteUid: String = String.empty(),
                     var name: String = String.empty(),
                     var thumbnailStatus: String = String.empty(),
                     var tempPath: String = String.empty(),
                     var originalPath: String = String.empty(),
                     var size: Long = 0L,
                     var originalStatus: String = String.empty(),
                     var type: String = String.empty(),
                     var backupStatus: String = String.empty(),
                     var date: Long = 0L) {

    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
            "uid" to uid,
            "remoteUid" to remoteUid,
            "name" to name,
            "thumbnailStatus" to thumbnailStatus,
            "tempPath" to tempPath,
            "size" to size,
            "originalStatus" to originalStatus,
            "originalPath" to originalPath,
            "type" to type,
            "backupStatus" to backupStatus,
            "date" to date)

    companion object {
        const val COLUMN_UID = "uid"
        const val COLUMN_REMOTE_UID = "remoteuid"
        const val COLUMN_NAME = "name"
        const val COLUMN_THUMBNAIL_STATUS = "thumbnailstatus"
        const val COLUMN_TEMP_PATH = "temppapth"
        const val COLUMN_ORIGINAL_PATH = "originalpath"
        const val COLUMN_SIZE = "size"
        const val COLUMN_ORIGINAL_STATUS = "ptu"
        const val COLUMN_TYPE = "type"
        const val COLUMN_BACKUP_STATUS = "backupstatus"
        const val COLUMN_DATE = "date"

        const val TABLE_NAME = "candidate"

        const val ORIGINAL_UPLOAD_NEED = "original_upload_need"
        const val ORIGINAL_UPLOAD_DONE = "original_upload_done"
        const val ORIGINAL_UPLOAD_FAIL = "original_upload_fail"
        const val ORIGINAL_UPLOAD_READE = "original_upload_reade"
        const val ORIGINAL_FILE_REMOVED = "original_file_removed"

        const val IMAGE_TYPE = "image"
        const val VIDEO_TYPE = "video"

        const val NEED_BACKUP = "original_need_backup"
        const val NO_BACKUP = "original_no_backup"
        const val BACKUP_NEED_REMOVE = "original_remove_backup"
        const val BACKUP_READE = "original_reade_backup"

        const val THUMBNAIL_UPLOAD_DONE = "thumbnail_upload_done"
        const val THUMBNAIL_UPLOAD_FAIL = "thumbnail_upload_fail"
        const val THUMBNAIL_UPLOAD_NEED = "thumbnail_upload_need"
    }
}