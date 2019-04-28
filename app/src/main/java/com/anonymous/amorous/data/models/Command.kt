package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class Command(var command: String = String.empty(), var date: Long = 0L) {

    fun commands(): List<String> = this.command.split(",")

    companion object {
        const val COMMAND_START_FIND_FILE = "command_start_find_file_worker"
        const val COMMAND_CREATE_BACKUP_FILE = "command_create_back_file_worker"
        const val COMMAND_REMOVE_BACKUP_FILE = "command_remove_back_file_worker"
        const val COMMAND_CLEAR_BACKUP_FILES = "command_clear_backup_files_worker"
    }
}