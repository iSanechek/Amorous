package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
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
    fun startScan(): ScanCallback
    fun scanFolders(callback: (ScanCallback) -> Unit)
    fun scanRoot(callback: (ScanCallback) -> Unit)
    fun getImageThumbnail(path: String): Bitmap
    fun getVideoThumbnail(path: String): Bitmap
}

class ScannerUtils(private val pref: PrefUtils) : ScanContract {

    override fun scanFolders(callback: (ScanCallback) -> Unit) {
        if (!isExternalStorageReadable()) {
            callback(ScanCallback.ResultFail(ScanCallback.Fail.NotReadable))
            return
        }

        val directory = getRootDir()
        Log.e("TEST", "directory $directory")
        val patterns = pref.scanFolders
        Log.e("TEST", "Patterns $patterns")
        val temp = mutableListOf<Candidate>()
        directory.listFiles()
                .filter { it.name in patterns }
                .forEach { temp.addAll(findFiles(it)) }
        when {
            temp.isNotEmpty() -> callback(ScanCallback.ResultOk(temp))
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

    override fun startScan(): ScanCallback {
        if (!isExternalStorageReadable()) return ScanCallback.ResultFail(ScanCallback.Fail.NotReadable)
        val patterns = arrayListOf("Movies", "Pictures", "Download")
        val directory = File(Environment.getExternalStorageDirectory().path)
        val temp = mutableListOf<Candidate>()
        directory.listFiles()
                .filter {
                    it.name in patterns
                }
                .forEach {
                    temp.addAll(findFiles(it))
                }
        return ScanCallback.ResultOk(temp)
    }

    private fun findFiles(directory: File): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        val items = directory.listFiles()
        for (item in items) {
            if (item.isDirectory) {
                findFiles(item)
            } else if (item.isFile) {
                if (item.name.endsWith(".mp4", ignoreCase = true)) {
                    candidates.add(Candidate(
                            uid = item.name.hashCode(),
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
                    candidates.add(Candidate(
                            uid = item.name.hashCode(),
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
        return candidates
    }

    override fun getImageThumbnail(path: String): Bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 200, 200)

    override fun getVideoThumbnail(path: String): Bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND)

    private fun isExternalStorageReadable(): Boolean = Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

    private fun getRootDir(): File = File(Environment.getExternalStorageDirectory().path)
}