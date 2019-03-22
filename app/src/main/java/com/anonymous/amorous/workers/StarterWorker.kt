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
) : Worker(context, parameters), KoinComponent {

    private val manager: WorkersManager by inject()
    private val config: ConfigurationUtils by inject()

    override fun doWork(): Result {
        when {
            config.getWorkerStatus() -> manager.startGeneralWorkers()
            else -> manager.stopAllWorkers()
        }
        return Result.success()
    }
}