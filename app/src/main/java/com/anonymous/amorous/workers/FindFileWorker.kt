package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.ScanCallback
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.*
import org.koin.standalone.inject

class FindFileWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result {
        val cache = db.getCandidates("originalStatus", "original_file_need_search")
        if (cache.isEmpty()) {
            when(val callback = scanner.scanFolders()) {
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
                is ScanCallback.ResultFail -> addEvent(TAG, "Скинирование папок завершено с ошибкой! ${callback.fail}")
            }
        }
        return Result.success()
    }

    private suspend fun scanRoot(candidate: Candidate) = when(val callback = scanner.scanRoot()) {
        is ScanCallback.ResultOk -> callback.items.filter { it.name == candidate.name }.forEach { actionUpload(it) }
        is ScanCallback.ResultFail -> addEvent(TAG, "Сканирование рут директории закончилось с ошибкой! ${callback.fail}")
        else -> addEvent(TAG, "Сканирование рут директории закончилось хуй пойми как.")
    }

    private suspend fun actionUpload(candidate: Candidate) {
        val c = upload.uploadOriginal(candidate)
        db.updateCandidate(
                uid = c.uid,
                column1 = "originalStatus",
                value1 = c.originalStatus,
                column2 = "originalRemoteUrl",
                value2 = c.originalRemoteUrl
        )
    }

    companion object {
        private const val TAG = "FindFileWorker"
    }
}