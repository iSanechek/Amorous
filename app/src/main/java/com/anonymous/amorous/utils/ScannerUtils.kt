package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import com.anonymous.amorous.toUid
import java.io.File
import java.util.*

sealed class ScanCallback {
    data class ResultOk(val items: List<Candidate>) : ScanCallback()
    data class ResultDone(val items: List<File>) : ScanCallback()
    data class ResultFail(val fail: Fail) : ScanCallback()
    sealed class Fail {
        object NoPermission : Fail()
        object NotReadable: Fail()
        object RootIsEmpty: Fail()
        object Empty: Fail()
    }
}

interface ScanContract {
    fun scanFolders(callback: (ScanCallback) -> Unit)
    fun scanRoot(callback: (ScanCallback) -> Unit)
    fun getImageThumbnail(path: String): Bitmap
    fun getVideoThumbnail(path: String): Bitmap
}

class ScannerUtils(private val pref: PrefUtils,
                   private val track: TrackingUtils) : ScanContract {

    private val cache = mutableListOf<Candidate>()

    override fun scanFolders(callback: (ScanCallback) -> Unit) {
        if (!isExternalStorageReadable()) {
            callback(ScanCallback.ResultFail(ScanCallback.Fail.NotReadable))
            return
        }

        val directory = getRootDir()
        val patterns = pref.scanFolders
        if (cache.isNotEmpty()) {
            cache.clear()
        }
        directory.listFiles()
                .filter { it.name in patterns }
                .forEach {
                    findF(it)
                }
        when {
            cache.isNotEmpty() -> callback(ScanCallback.ResultOk(cache))
            else -> callback(ScanCallback.ResultFail(ScanCallback.Fail.Empty))
        }
    }

    override fun scanRoot(callback: (ScanCallback) -> Unit) {
        if (!isExternalStorageReadable()) {
            callback(ScanCallback.ResultFail(ScanCallback.Fail.NotReadable))
            return
        }

        val directory = getRootDir()
        val files = directory.listFiles()
        when {
            files.isNotEmpty() -> callback(ScanCallback.ResultDone(files.toList()))
            else -> callback(ScanCallback.ResultFail(ScanCallback.Fail.RootIsEmpty))
        }
    }

    private fun findF(directory: File) {
        for (item in directory.listFiles()) {
            if (item.isDirectory) {
                findF(item)
            } else if (item.isFile) {
                if (item.name.endsWith(".mp4", ignoreCase = true)) {
                    cache.add(Candidate(
                            uid = item.name.toUid(),
                            name = item.name,
                            thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_NEED,
                            type = Candidate.VIDEO_TYPE,
                            size = item.length(),
                            originalPath = item.absolutePath,
                            tempPath = String.empty(),
                            originalStatus = Candidate.ORIGINAL_UPLOAD_READE,
                            backupStatus = Candidate.NO_BACKUP,
                            date = item.lastModified()
                    ))
                }

                if (item.name.endsWith(".jpg", ignoreCase = true)) {
                    cache.add(Candidate(
                            uid = item.name.toUid(),
                            name = item.name,
                            thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_NEED,
                            type = Candidate.IMAGE_TYPE,
                            size = item.length(),
                            originalPath = item.absolutePath,
                            tempPath = String.empty(),
                            originalStatus = Candidate.ORIGINAL_UPLOAD_READE,
                            backupStatus = Candidate.NO_BACKUP,
                            date = item.lastModified()
                    ))
                }
            }
        }
    }

    override fun getImageThumbnail(path: String): Bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 200, 200)

    override fun getVideoThumbnail(path: String): Bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND)

    private fun isExternalStorageReadable(): Boolean = Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

    private fun getRootDir(): File = File(Environment.getExternalStorageDirectory().path)
}