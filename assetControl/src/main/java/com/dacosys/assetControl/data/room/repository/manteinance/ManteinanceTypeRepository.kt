package com.dacosys.assetControl.data.room.repository.manteinance

import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceTypeDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ManteinanceTypeRepository {
    private val dao: ManteinanceTypeDao by lazy {
        database.manteinanceTypeDao()
    }

    fun getAllManteinanceTypes(): Flow<List<ManteinanceType>> = dao.getAllManteinanceTypes()

    suspend fun insertManteinanceType(manteinanceType: ManteinanceType) {
        withContext(Dispatchers.IO) {
            dao.insertManteinanceType(manteinanceType)
        }
    }

    suspend fun insertAll(manteinanceTypeList: List<ManteinanceType>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(manteinanceTypeList)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}