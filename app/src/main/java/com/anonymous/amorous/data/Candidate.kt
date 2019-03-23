package com.anonymous.amorous.data

import com.anonymous.amorous.empty
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//
@IgnoreExtraProperties
data class Candidate(var uid: Int? = 0,
                     var name: String? = String.empty(),
                     var thumbnailStatus: String? = String.empty(),
                     var tempPath: String? = String.empty(),
                     var originalPath: String? = String.empty(),
                     var size: String? = String.empty(),
                     var originalStatus: String? = String.empty(),
                     var type: String? = String.empty(),
                     var backupStatus: String? = String.empty(),
                     var date: Long? = 0L) {

    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
            "uid" to uid,
            "name" to name,
            "thumbnailStatus" to thumbnailStatus,
            "tempPath" to tempPath,
            "size" to size,
            "originalStatus" to originalStatus,
            "type" to type,
            "backupStatus" to backupStatus,
            "date" to date)

    companion object {
        const val COLUMN_UID = "u"
        const val COLUMN_NAME = "n"
        const val COLUMN_THUMBNAIL_STATUS = "r_c_u"
        const val COLUMN_TEMP_PATH = "t_l_b_p"
        const val COLUMN_ORIGINAL_PATH = "o_b_p"
        const val COLUMN_SIZE = "s"
        const val COLUMN_ORIGINAL_STATUS = "n_o_u"
        const val COLUMN_TYPE = "t"
        const val COLUMN_BACKUP_STATUS = "n_l_b"
        const val COLUMN_DATE = "d"

        const val TABLE_NAME = "c"

        const val ORIGINAL_UPLOAD_NEED = "original_upload_need"
        const val ORIGINAL_UPLOAD_DONE = "original_upload_done"
        const val ORIGINAL_UPLOAD_FAIL = "original_upload_fail"
        const val ORIGINAL_UPLOAD_READE = "original_upload_reade"
        const val ORIGINAL_UPLOAD_REMOVE = "original_upload_remove"

        const val IMAGE_TYPE = "image"
        const val VIDEO_TYPE = "video"

        const val ORIGINAL_NEED_BACKUP = "original_need_backup"
        const val ORIGINAL_NO_BACKUP = "original_no_backup"
        const val ORIGINAL_NEED_REMOVE = "original_remove_backup"
        const val ORIGINAL_BACKUP_READE = "original_reade_backup"

        const val THUMBNAIL_UPLOAD_DONE = "thumbnail_upload_done"
        const val THUMBNAIL_UPLOAD_FAIL = "thumbnail_upload_fail"
        const val THUMBNAIL_UPLOAD_NEED = "thumbnail_upload_need"
    }
}