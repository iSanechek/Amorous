package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import java.util.*

data class Candidate(var uid: String = String.empty(),
                     var remoteUid: String = String.empty(),
                     var name: String = String.empty(),
                     var thumbnailStatus: String = String.empty(),
                     var thumbnailRemoteUrl: String = String.empty(),
                     var originalRemoteUrl: String = String.empty(),
                     var tempPath: String = String.empty(),
                     var originalPath: String = String.empty(),
                     var size: Long = 0L,
                     var originalStatus: String = String.empty(),
                     var type: String = String.empty(),
                     var backupStatus: String = String.empty(),
                     var date: Long = 0L) {

    fun toMap(): Map<String, Any?> = mapOf(
            "uid" to uid,
            "remoteUid" to remoteUid,
            "name" to name,
            "thumbnailStatus" to thumbnailStatus,
            "thumbnailRemoteUrl" to thumbnailRemoteUrl,
            "originalRemoteUrl" to originalRemoteUrl,
            "tempPath" to tempPath,
            "size" to size,
            "originalStatus" to originalStatus,
            "originalPath" to originalPath,
            "type" to type,
            "backupStatus" to backupStatus,
            "date" to date)

    companion object {

        const val ORIGINAL_UPLOAD_NEED = "original_upload_need"
        const val ORIGINAL_UPLOAD_DONE = "original_upload_done"
        const val ORIGINAL_UPLOAD_FAIL = "original_upload_fail"
        const val ORIGINAL_UPLOAD_FILE_ERROR = "original_upload_fail_error"
        const val ORIGINAL_FILE_NOT_EXISTS = "original_file_not_exists"
        const val ORIGINAL_FILE_TYPE_ERROR = "original_file_type_error"
        const val ORIGINAL_UPLOAD_READE = "original_upload_reade"
        const val ORIGINAL_FILE_REMOVED = "original_file_removed"
        const val ORIGINAL_FILE_NEED_SEARCH = "original_file_need_search"

        const val IMAGE_TYPE = "image"
        const val VIDEO_TYPE = "video"

        const val NEED_BACKUP = "original_need_backup"
        const val NO_BACKUP = "original_no_backup"
        const val BACKUP_NEED_REMOVE = "original_remove_backup"
        const val BACKUP_READE = "original_reade_backup"

        const val THUMBNAIL_UPLOAD_DONE = "thumbnail_upload_done"
        const val THUMBNAIL_UPLOAD_NEED = "thumbnail_upload_need"
        const val THUMBNAIL_UPLOAD_TYPE_ERROR = "thumbnail_upload_type_error"
        const val THUMBNAIL_UPLOAD_FILE_NOT_EXISTS = "thumbnail_upload_file_not_exists"
        const val THUMBNAIL_UPLOAD_FILE_ERROR = "thumbnail_upload_file_error"
        const val THUMBNAIL_UPLOAD_FILE_PAT_ERROR = "thumbnail_upload_file_path_error"

        fun getUploadStatus(value: String): String = when(value) {
            ORIGINAL_UPLOAD_NEED -> "нужно"
            ORIGINAL_UPLOAD_READE -> "готов"
            ORIGINAL_UPLOAD_FAIL -> "ошибка"
            ORIGINAL_FILE_REMOVED -> "удален"
            ORIGINAL_UPLOAD_DONE -> "загружен"
            else -> "Хуйня какая-та. $value"
        }
    }
}