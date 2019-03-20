package com.anonymous.amorous.data

data class Task(
        val id: String,
        val name: String,
        val startTime: Long,
        val finishTime: Long,
        val status: String,
        val message: String,
        val candidateId: String
) {
    companion object {
        const val TABLE_NAME = "task"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_START = "start_time"
        const val COLUMN_FINISH = "finish_time"
        const val COLUMN_STATUS = "status"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_CANDIDATE_ID = "candidate_id"

        const val STATUS_READY = "ready"
        const val STATUS_FAIL = "fail"
        const val STATUS_DONE = "done"
    }
}