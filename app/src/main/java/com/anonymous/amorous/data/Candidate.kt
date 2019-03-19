package com.anonymous.amorous.data

import com.anonymous.amorous.empty
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//
@IgnoreExtraProperties
data class Candidate(var uid: Int? = 0,
                     var name: String? = String.empty(),
                     var remoteCoverUrl: String? = String.empty(),
                     var tempLocalBitmapPath: String? = String.empty(),
                     var originalLocalBitmapPath: String? = String.empty(),
                     var size: String? = String.empty(),
                     var needOriginalUpload: String? = String.empty(),
                     var type: String? = String.empty(),
                     var needLocalBackup: String? = String.empty()) {

    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
            "uid" to uid,
            "name" to name,
            "remoteCoverUrl" to remoteCoverUrl,
            "tempLocalBitmapPath" to tempLocalBitmapPath,
            "size" to size,
            "needOriginalUpload" to needOriginalUpload,
            "type" to type,
            "needLocalBackup" to needLocalBackup)

    companion object {
        const val COLUMN_UID = "u"
        const val COLUMN_NAME = "n"
        const val COLUMN_REMOTE_COVER_URL = "r_c_u"
        const val COLUMN_TEMP_LOCAL_BITMAP_PATH = "t_l_b_p"
        const val COLUMN_ORIGINAL_BITMAP_PATH = "o_b_p"
        const val COLUMN_SIZE = "s"
        const val COLUMN_NEED_ORIGINAL_UPLOAD= "n_o_u"
        const val COLUMN_TYPE = "t"
        const val COLUMN_NEED_LOCAL_BACKUP = "n_l_b"

        const val TABLE_NAME = "c"

        const val ORIGINAL_FILE_UPLOAD_NEED = "original.upload.need"
        const val ORIGINAL_FILE_UPLOAD_DONE = "original.upload.done"
        const val ORIGINAL_FILE_UPLOAD_FAIL = "original.upload.fail"
        const val ORIGINAL_FILE_UPLOAD_READE = "original.upload.reade"

        const val IMAGE_TYPE = "image"
        const val VIDEO_TYPE = "video"

        const val ORIGINAL_FILE_NEED_BACKUP = "original.need.backup"
        const val ORIGINAL_FILE_NO_BACKUP = "original.no.backup"
        const val ORIGINAL_FILE_NEED_REMOVE = "original.remove.backup"
        const val ORIGINAL_FILE_BACKUP_READE = "original.reade.backup"
    }
}