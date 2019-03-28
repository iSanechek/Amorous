package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import java.io.File
import java.util.*

sealed class ScanCallback {
    data class ResultOk(val items: List<Candidate>) : ScanCallback()
    data class ResultFail(val fail: Fail) : ScanCallback()
    sealed class Fail {
        object NoPermission : Fail()
        object NotReadable: Fail()
    }
}

interface ScanContract {
    fun startScan(): ScanCallback
    fun getImageThumbnail(path: String): Bitmap
    fun getVideoThumbnail(path: String): Bitmap
}

class ScannerUtils(
        private val fileUtil: FileUtils
) : ScanContract {

    override fun startScan(): ScanCallback {
        val isExternalStorageReadable: Boolean = Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
        if (!isExternalStorageReadable) return ScanCallback.ResultFail(ScanCallback.Fail.NotReadable)
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
}