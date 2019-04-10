package com.anonymous.amorous.data.database

import com.android.camera.storage.dao.CandidateDao
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Event
import com.anonymous.amorous.utils.fromModel
import com.anonymous.amorous.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageImpl(private val dao: CandidateDao) : LocalDatabase {
    override fun updateThumbnailsStatus(uid: String, status: String) {
        dao.updateThumbnailsStatus(uid, status)
    }

    override suspend fun getThumbnailsCandidate(parameter: String, limit: Int): List<Candidate> = withContext(Dispatchers.IO) {
        dao.getThumbnails(parameter, limit).map { it.fromModel() }.toList()
    }

    override suspend fun getOriginalsCandidate(parameter: String): List<Candidate> = withContext(Dispatchers.IO) {
        val limit = 5
        dao.getOriginals(parameter, limit).map { it.fromModel() }.toList()
    }

    override suspend fun getBackupCandidate(parameter: String): List<Candidate> = withContext(Dispatchers.IO) {
        dao.getBackup(parameter).map { it.fromModel() }.toList()
    }

    override suspend fun saveCandidates(items: List<Candidate>) {
        val cache = getCandidates()
        val temp = mutableListOf<Candidate>()
        if (cache.isNotEmpty()) {
            for (item in items) {
                val c = cache.find { it.uid == item.uid }
                if (c == null) {
                    temp.add(item)
                }
            }

            if (temp.isNotEmpty()) {
                dao.insert(temp.map { it.toModel() }.toList())
            }
        }


        dao.insert(items.map { it.toModel() }.toList())
    }

    override fun saveCandidate(candidate: Candidate) {
        dao.insert(candidate.toModel())
    }

    override suspend fun updateCandidate(item: Candidate) = withContext(Dispatchers.IO) {
        dao.update(item.toModel())
    }

    override suspend fun getCandidates(): List<Candidate> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.fromModel() }.toList()
    }

    override fun getCandidate(id: String): Candidate = dao.getCandidate(id).fromModel()

    override suspend fun removeCandidate(candidate: Candidate) = withContext(Dispatchers.IO) {
        dao.delete(candidate.uid)
    }

    override fun clearDb() {
        dao.deleteAll()
    }

    override fun getCandidates(select: String, args: Array<String>?): List<Candidate> = emptyList()

    override fun saveEvent(event: Event) {

    }

    override fun getEvents(): List<Event> = emptyList()

    override fun clearEvents(ids: Set<String>) {

    }

}