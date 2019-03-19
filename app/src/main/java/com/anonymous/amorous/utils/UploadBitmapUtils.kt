package com.anonymous.amorous.utils

import android.graphics.Bitmap
import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.User
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.debug.logDebug
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

interface UploadBitmapUtils {
    fun uploadBitmap(candidate: Candidate)
}

class UploadBitmapUtilsImpl(
        private val scanner: ScanContract,
        private val database: DatabaseContract,
        private val localDb: LocalDatabase
) : UploadBitmapUtils {

    override fun uploadBitmap(candidate: Candidate) {

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference

        if (candidate.needOriginalUpload == Candidate.ORIGINAL_FILE_UPLOAD_NEED) {
            logDebug {
                "Upload original ${candidate.name}"
            }

            val sr = imageRef.child("$O_R_F_N/${candidate.name}")
            val stream = FileInputStream(File(candidate.originalLocalBitmapPath))
            val ut = sr.putStream(stream)
            ut.addOnFailureListener {
                logDebug {
                    "Upload original error ${it.message}"
                }
            }.addOnSuccessListener {
                logDebug {
                    "Upload original done! ${it.metadata?.name}"
                }
                writeInDatabase(candidate.copy(needOriginalUpload = Candidate.ORIGINAL_FILE_UPLOAD_DONE))
            }
        } else {
            logDebug {
                "Upload Thumbnail ${candidate.name}"
            }
            val sr = imageRef.child("$T_R_F_N/${candidate.name}.jpg")
            val bitmap = when {
                candidate.type == Candidate.IMAGE_TYPE -> scanner.getImageThumbnail(candidate.originalLocalBitmapPath!!)
                else -> scanner.getVideoThumbnail(candidate.originalLocalBitmapPath!!)
            }
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = sr.putBytes(data)
            uploadTask.addOnCompleteListener {
                if (it.isSuccessful) {
                    logDebug {
                        "Upload Successful "
                    }
                    writeInDatabase(candidate)
                }
            }.addOnFailureListener {
                logDebug {
                    "Upload fail"
                }
            }
        }
    }

    private fun writeInDatabase(candidate: Candidate) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser ?: return
        database.getDatabase()
                .child(DB_T_U)
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        logDebug {
                            "User onCancelled ${p0.toException()}"
                        }
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.getValue(User::class.java) != null) {
                            database.writeCandidateInDatabase(currentUser.uid, candidate)

                            GlobalScope.launch(Dispatchers.IO) {
                                localDb.updateCandidate(candidate)
                            }
                        } else {
                            logDebug {
                                "User is null! Abort write new candidate!"
                            }
                        }
                    }
                })
    }

    companion object {
        private const val T_R_F_N = "thumbnails"
        private const val O_R_F_N = "originals"
    }
}