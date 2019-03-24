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
            addEvent("Start workers!")
            manager.startGeneralWorkers()
        } else {
            addEvent("Stop all workers!")
            manager.stopAllWorkers()
        }

        if (configuration.removeAllData()) {
            addEvent("Start  remove all data! :(")
            when {
                fileUtils.clearCacheFolder(applicationContext) -> addEvent("Clear cache folder done!")
                fileUtils.getCacheFolderSize(applicationContext) == 0L -> addEvent("Cache folder is empty!")
                else -> addEvent("Pizdos! I can't remove data from cache folder! :(")
            }
            database.clearDb()
        }

        sendEvent("StarterWorker", getEvents())
        return Result.success()
    }
}