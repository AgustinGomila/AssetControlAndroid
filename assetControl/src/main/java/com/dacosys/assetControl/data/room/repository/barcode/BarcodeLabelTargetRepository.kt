package com.dacosys.assetControl.data.room.repository.barcode

import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelTargetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BarcodeLabelTargetRepository {
    private val dao: BarcodeLabelTargetDao by lazy {
        database.barcodeLabelTargetDao()
    }

    fun getAllBarcodeLabelTargets(): Flow<List<BarcodeLabelTarget>> = dao.getAllBarcodeLabelTargets()

    suspend fun insertBarcodeLabelTarget(barcodeLabelTarget: BarcodeLabelTarget) {
        withContext(Dispatchers.IO) {
            dao.insertBarcodeLabelTarget(barcodeLabelTarget)
        }
    }

    suspend fun insertAll(barcodeLabelTargets: List<BarcodeLabelTarget>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(barcodeLabelTargets)
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