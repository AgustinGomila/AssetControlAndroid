package com.dacosys.assetControl.data.room.repository.manteinance

import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceStatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ManteinanceStatusRepository {
    private val dao: ManteinanceStatusDao by lazy {
        database.manteinanceStatusDao()
    }

    fun getAllManteinanceStatus(): Flow<List<ManteinanceStatus>> = dao.getAllManteinanceStatus()

    suspend fun insertManteinanceStatus(manteinanceStatus: ManteinanceStatus) {
        withContext(Dispatchers.IO) {
            dao.insertManteinanceStatus(manteinanceStatus)
        }
    }

    suspend fun insertAll(manteinanceStatusList: List<ManteinanceStatus>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(manteinanceStatusList)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}