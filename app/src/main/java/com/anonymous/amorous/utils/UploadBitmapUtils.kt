package com.anonymous.amorous.utils

import android.graphics.Bitmap
import com.anonymous.amorous.data.models.Candidate
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

interface UploadBitmapUtils {
    fun uploadBitmap(candidate: Candidate, callback: (Candidate) -> Unit)
}

class UploadBitmapUtilsImpl(
        private val scanner: ScanContract,
        private val tracker: TrackingUtils
) : UploadBitmapUtils {

    private val events = hashSetOf<String>()

    override fun uploadBitmap(candidate: Candidate, callback: (Candidate) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference
        when {
            candidate.originalStatus == Candidate.ORIGINAL_UPLOAD_NEED -> {
                addEvent("Start upload original for ${candidate.name}")
                val sr = imageRef.child("$O_R_F_N/${candidate.name}")
                val stream = FileInputStream(File(candidate.originalPath))
                val ut = sr.putStream(stream)
                ut.addOnFailureListener {
                    addEvent("Upload original error ${it.message}")
                    tracker.sendEvent(TAG, events)
                    callback(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_FAIL))
                }.addOnSuccessListener {
                    addEvent("Upload original done! ${it.metadata?.name}")
                    tracker.sendEvent(TAG, events)
                    callback(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_DONE))
                }
            }
            else -> {
                addEvent("Upload Thumbnail ${candidate.name}")
                val sr = imageRef.child("$T_R_F_N/${candidate.name}.jpg")
                val bitmap = when {
                    candidate.type == Candidate.IMAGE_TYPE -> scanner.getImageThumbnail(candidate.originalPath!!)
                    else -> scanner.getVideoThumbnail(candidate.originalPath!!)
                }
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uploadTask = sr.putBytes(data)
                uploadTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        addEvent("Upload thumbnail for ${candidate.name} is done!")
                        tracker.sendEvent(TAG, events)
                        callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_DONE))
                    }
                }.addOnFailureListener {
                    addEvent("Upload thumbnail for ${candidate.name} is fail!")
                    tracker.sendEvent(TAG, events)
                    callback(candidate.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_FAIL))
                }
            }
        }
    }

    private fun addEvent(event: String) {
        events.add(event)
    }

    companion object {
        private const val T_R_F_N = "thumbnails"
        private const val O_R_F_N = "originals"
        private const val TAG = "UploadBitmapUtils"
    }
}