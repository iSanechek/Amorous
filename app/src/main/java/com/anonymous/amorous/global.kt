package com.anonymous.amorous

fun String.Companion.empty() = ""

const val DB_T_U = "users"
const val DB_T_C = "candidates"
const val DB_T_E = "events"
const val DB_T_M = "messages"
const val DB_T_I = "info"

const val WORKER_RETRY_VALUE_KEY = "worker_retry_value"
const val WORKER_SCAN_TIME_KEY = "time_for_scan_worker"
const val WORKER_SYNC_TIME_KEY = "time_for_sync_worker"
const val WORKER_CHECKER_TIME_KEY = "time_for_checker_worker"
const val WORKER_ORIGINAL_TIME_KEY = "time_for_original_worker"
const val WORKER_THUMBNAIL_TIME_KEY = "time_for_thumbnail_worker"
const val WORKER_GENERAL_TIME_KEY = "time_for_general_worker"
const val CANDIDATE_REMOTE_TABLE_KEY = "candidate_remote_table_key"
const val JOBS_SERVICE_STATUS_KEY = "jobs_server_start"
