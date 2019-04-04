package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import com.anonymous.amorous.data.models.Candidate
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

interface UploadBitmapUtils {
    fun uploadThumbnail(candidate: Candidate, callback: (Candidate) -> Unit)
    fun uploadOriginal(candidate: Candidate, callback: (Candidate) -> Unit)
}

class UploadBitmapUtilsImpl(
        private val tracker: TrackingUtils,
        private val fileUtils: FileUtils
) : UploadBitmapUtils {

    override fun uploadThumbnail(candidate: Candidate, callback: (Candidate) -> Unit) {
        addEvent("Start upload thumbnail for ${candidate.name}!")
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference
        val path = getPath(candidate)

        val patterns = setOf(".mp4", ".jpg")
        val type = path.replaceBeforeLast(".", "")
        if (type !in patterns) {
            callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FAIL))
            return
        }

        if (!fileUtils.checkFileExists(path)) {
            callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FAIL))
            return
        }

        val sr = imageRef.child("$T_R_F_N/${path.substring(path.lastIndexOf("/") + 1)}")
        if (path.isNotEmpty()) {
            val bitmap = when {
                candidate.type == Candidate.VIDEO_TYPE -> getVideoThumbnail(path)
                else -> getImageThumbnail(path)
            }
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = sr.putBytes(data)
            uploadTask.addOnSuccessListener {
                addEvent("Upload thumbnail for ${candidate.name} is done!")
                callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_DONE))
            }.addOnFailureListener {
                addEvent("Upload thumbnail for ${candidate.name} is fail!")
                callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FAIL))
            }
        } else {
            addEvent("Upload thumbnail for ${candidate.name} is fail! Path is null!")
            callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FAIL))
        }
    }

    override fun uploadOriginal(candidate: Candidate, callback: (Candidate) -> Unit) {
        addEvent("Start upload original for ${candidate.name}!")
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference
        val sr = imageRef.child("$O_R_F_N/${candidate.name}")

        val path = getPath(candidate)
        val patterns = setOf(".mp4", ".jpg")
        val type = path.replaceBeforeLast(".", "")
        if (type !in patterns) {
            callback(candidate.copy(thumbnailStatus = Candidate.ORIGINAL_UPLOAD_FAIL))
            return
        }

        if (!fileUtils.checkFileExists(path)) {
            callback(candidate.copy(thumbnailStatus = Candidate.ORIGINAL_UPLOAD_FAIL))
            return
        }

        val stream = FileInputStream(File(path))
        val ut = sr.putStream(stream)
        ut.addOnFailureListener {
            addEvent("Upload original error ${it.message}!")
            tracker.sendOnServer()
            callback(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_FAIL))
        }.addOnSuccessListener {
            addEvent("Upload original done! ${it.metadata?.name}")
            tracker.sendOnServer()
            callback(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_DONE))
        }
    }

    private fun addEvent(event: String) {
        tracker.sendEvent(TAG, event)
    }

    private fun getPath(candidate: Candidate): String = if (candidate.tempPath.isNotEmpty()) candidate.tempPath else candidate.originalPath

    private fun getVideoThumbnail(path: String): Bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND)

    private fun getImageThumbnail(path: String): Bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 200, 200)

    companion object {
        private const val T_R_F_N = "thumbnails"
        private const val O_R_F_N = "originals"
        private const val TAG = "UploadBitmapUtils"
    }
}