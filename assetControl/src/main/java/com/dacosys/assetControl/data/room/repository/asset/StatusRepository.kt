package com.dacosys.assetControl.data.room.repository.asset

import com.dacosys.assetControl.data.room.dao.asset.StatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.asset.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusRepository {
    private val dao: StatusDao by lazy {
        database.statusDao()
    }

    fun getAllStatuses() = dao.getAllStatuses()

    fun getById(id: Int) = dao.getById(id)

    suspend fun insertStatus(status: Status) {
        withContext(Dispatchers.IO) {
            dao.insertStatus(status)
        }
    }

    suspend fun insertAll(statuses: List<Status>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(statuses)
        }
    }

    suspend fun deleteById(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
