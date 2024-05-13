package com.dacosys.assetControl.data.room.repository.manteinance

import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceTypeGroupDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceTypeGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ManteinanceTypeGroupRepository {
    private val dao: ManteinanceTypeGroupDao by lazy {
        database.manteinanceTypeGroupDao()
    }

    fun getAllManteinanceTypeGroups(): Flow<List<ManteinanceTypeGroup>> = dao.getAllManteinanceTypeGroups()

    suspend fun insertManteinanceTypeGroup(manteinanceTypeGroup: ManteinanceTypeGroup) {
        withContext(Dispatchers.IO) {
            dao.insertManteinanceTypeGroup(manteinanceTypeGroup)
        }
    }

    suspend fun insertAll(manteinanceTypeGroups: List<ManteinanceTypeGroup>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(manteinanceTypeGroups)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
