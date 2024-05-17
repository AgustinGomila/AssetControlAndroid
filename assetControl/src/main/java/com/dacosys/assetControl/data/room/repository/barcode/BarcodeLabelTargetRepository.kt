package com.dacosys.assetControl.data.room.repository.barcode

import com.dacosys.assetControl.data.enums.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelTargetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import kotlinx.coroutines.runBlocking
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget as BarcodeLabelTargetRoom

class BarcodeLabelTargetRepository {
    private val dao: BarcodeLabelTargetDao
        get() = database.barcodeLabelTargetDao()

    fun sync() = runBlocking {
        val targets = BarcodeLabelTarget.getAll().map { BarcodeLabelTargetRoom(it) }.toList()
        dao.deleteAll()
        dao.insert(targets)
    }
}