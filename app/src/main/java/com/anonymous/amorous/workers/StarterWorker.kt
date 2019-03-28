package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.ConfigurationUtils
import com.anonymous.amorous.utils.WorkersManager
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class StarterWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    private val manager: WorkersManager by inject()

    override suspend fun doWorkAsync(): Result {

        if (configuration.getWorkerStatus()) {
            sendEvent("StarterWorker", "Start workers!")
            manager.startGeneralWorkers()
        } else {
            sendEvent("StarterWorker", "Stop all workers!")
            manager.stopAllWorkers()
        }

        if (configuration.removeAllData()) {
            sendEvent("StarterWorker", "Start  remove all data! :(")
            when {
                fileUtils.clearCacheFolder(applicationContext) -> sendEvent("StarterWorker", "Clear cache folder done!")
                fileUtils.getCacheFolderSize(applicationContext) == 0L -> sendEvent("StarterWorker", "Cache folder is empty!")
                else -> sendEvent("StarterWorker", "Pizdos! I can't remove data from cache folder! :(")
            }
            database.clearDb()
        }

        sendEvents()
        return Result.success()
    }
}