package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import com.anonymous.amorous.toUid
import java.io.File

sealed class ScanCallback {
    data class ResultOk(val items: List<Candidate>) : ScanCallback()
    data class ResultDone(val items: List<File>) : ScanCallback()
    data class ResultFail(val fail: Fail) : ScanCallback()
    sealed class Fail {
        object NoPermission : Fail()
        object NotReadable : Fail()
        object RootIsEmpty : Fail()
        object Empty : Fail()
    }
}

interface ScanContract {
    suspend fun scanFolders(): ScanCallback
    suspend fun scanRoot(): ScanCallback
    fun getImageThumbnail(path: String): Bitmap
    fun getVideoThumbnail(path: String): Bitmap
}

class ScannerUtils(private val configuration: ConfigurationUtils) : ScanContract {

    private val cache = mutableListOf<Candidate>()

    override suspend fun scanFolders(): ScanCallback {
        if (!isExternalStorageReadable()) {
            return ScanCallback.ResultFail(ScanCallback.Fail.NotReadable)
        } else {
            val directory = getRootDir()
            val patterns = configuration.getScanFoldersPattern()
//            val patterns = setOf("test_folder")
            if (cache.isNotEmpty()) {
                cache.clear()
            }
            directory.listFiles()
                    .filter { it.name in patterns }
                    .forEach {
                        findF(it)
                    }
            return when {
                cache.isNotEmpty() -> ScanCallback.ResultOk(cache)
                else -> ScanCallback.ResultFail(ScanCallback.Fail.Empty)
            }
        }
    }

    override suspend fun scanRoot(): ScanCallback {
        if (!isExternalStorageReadable()) return ScanCallback.ResultFail(ScanCallback.Fail.NotReadable)
        val directory = getRootDir()
        val files = directory.listFiles()
        return when {
            files.isNotEmpty() -> ScanCallback.ResultDone(files.toList())
            else -> ScanCallback.ResultFail(ScanCallback.Fail.RootIsEmpty)
        }
    }

    private fun findF(directory: File) {
        for (item in directory.listFiles()) {
            if (item.isDirectory) {
                if (item.name !in configuration.getNotScanFoldersPattern()) {
                    findF(item)
                }
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