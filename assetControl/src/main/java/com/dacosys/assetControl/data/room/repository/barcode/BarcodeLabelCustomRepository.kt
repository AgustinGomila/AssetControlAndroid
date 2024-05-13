package com.dacosys.assetControl.data.room.repository.barcode

import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelCustomDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BarcodeLabelCustomRepository {
    private val dao: BarcodeLabelCustomDao by lazy {
        database.barcodeLabelCustomDao()
    }

    fun getAllBarcodeLabels(): Flow<List<BarcodeLabelCustom>> = dao.getAllBarcodeLabels()

    suspend fun insertBarcodeLabel(barcodeLabel: BarcodeLabelCustom) {
        withContext(Dispatchers.IO) {
            dao.insertBarcodeLabel(barcodeLabel)
        }
    }

    suspend fun insertAll(barcodeLabels: List<BarcodeLabelCustom>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(barcodeLabels)
        }
    }

    suspend fun deleteById(id: Long) {
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