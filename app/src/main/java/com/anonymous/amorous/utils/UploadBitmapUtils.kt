package com.anonymous.amorous.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.anonymous.amorous.data.models.Candidate
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface UploadBitmapUtils {
    suspend fun uploadThumbnail(candidate: Candidate): Candidate
    suspend fun uploadOriginal(candidate: Candidate): Candidate
}

class UploadBitmapUtilsImpl(
        private val tracker: TrackingUtils,
        private val fileUtils: FileUtils,
        private val configuration: ConfigurationUtils
) : UploadBitmapUtils {

    override suspend  fun uploadThumbnail(candidate: Candidate): Candidate = suspendCoroutine { c ->
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference
        val path = getPath(candidate)
        val patterns = configuration.getFindSearchType()
        val type = path.replaceBeforeLast(".", "")
        if (type !in patterns) {
            c.resume(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_TYPE_ERROR))
            return@suspendCoroutine
        }
        if (!fileUtils.checkFileExists(path)) {
            c.resume(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FILE_NOT_EXISTS))
            return@suspendCoroutine
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

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation sr.downloadUrl
            }).addOnCompleteListener {
                if (it.isSuccessful) {
                    addEvent("Upload thumbnail for ${candidate.name} is done!")
                    val url = it.result.toString()
                    c.resume(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_DONE, thumbnailRemoteUrl = url))
                }
            }.addOnFailureListener {
                addEvent("Upload thumbnail for ${candidate.name} is error! ${it.message}")
                c.resume(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FILE_ERROR))
            }
        } else {
            addEvent("Upload thumbnail for ${candidate.name} is fail! Path is null!")
            c.resume(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FILE_PAT_ERROR))
        }
    }

    override suspend fun uploadOriginal(candidate: Candidate): Candidate = suspendCoroutine { c ->
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference
        val sr = imageRef.child("$O_R_F_N/${candidate.name}")
        val path = getPath(candidate)
        val patterns = configuration.getFindSearchType()
        val type = path.replaceBeforeLast(".", "")
        if (type !in patterns) {
            c.resume(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_TYPE_ERROR))
            return@suspendCoroutine
        }
        if (!fileUtils.checkFileExists(path)) {
            c.resume(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_NOT_EXISTS))
            return@suspendCoroutine
        }
        val stream = FileInputStream(File(path))
        val ut = sr.putStream(stream)
        ut.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation sr.downloadUrl
        }).addOnSuccessListener {
            addEvent("Upload original done!")
            c.resume(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_DONE, originalRemoteUrl = it.toString()))
        }.addOnFailureListener {
            addEvent("Upload original error ${it.message}!")
            c.resume(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_FILE_ERROR))
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