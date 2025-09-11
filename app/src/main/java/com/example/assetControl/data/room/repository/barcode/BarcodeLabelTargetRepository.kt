package com.example.assetControl.data.room.repository.barcode

import com.example.assetControl.data.enums.barcode.BarcodeLabelTarget
import com.example.assetControl.data.room.dao.barcode.BarcodeLabelTargetDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.entity.barcode.BarcodeLabelTargetEntity
import kotlinx.coroutines.runBlocking

class BarcodeLabelTargetRepository {
    private val dao: BarcodeLabelTargetDao
        get() = database.barcodeLabelTargetDao()

    fun sync() = runBlocking {
        val targets = BarcodeLabelTarget.getAll().map { BarcodeLabelTargetEntity(it) }.toList()
        dao.deleteAll()
        dao.insert(targets)
    }
}