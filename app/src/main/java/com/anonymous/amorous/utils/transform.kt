package com.anonymous.amorous.utils

import com.android.camera.storage.models.CandidateModel
import com.anonymous.amorous.data.models.Candidate

fun Candidate.toModel(): CandidateModel = CandidateModel(
        uid = uid,
        thumbnailStatus = thumbnailStatus,
        backupStatus = backupStatus,
        originalPath = originalPath,
        size = size,
        type = type,
        tempPath = tempPath,
        remoteUid = remoteUid,
        name = name,
        originalStatus = originalStatus,
        date = date
)
fun CandidateModel.fromModel(): Candidate = Candidate(
        uid = uid,
        thumbnailStatus = thumbnailStatus,
        backupStatus = backupStatus,
        originalPath = originalPath,
        size = size,
        type = type,
        tempPath = tempPath,
        remoteUid = remoteUid,
        name = name,
        originalStatus = originalStatus,
        date = date
)