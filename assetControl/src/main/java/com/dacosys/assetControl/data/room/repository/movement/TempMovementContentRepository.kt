package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.data.room.dao.movement.TempMovementContentDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContent
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import kotlinx.coroutines.runBlocking

class TempMovementContentRepository {
    private val dao: TempMovementContentDao
        get() = database.tempMovementContentDao()

    private val nextId: Long
        get() = runBlocking { (dao.selectMaxId() ?: 0) + 1 }

    fun selectByTempId(wmId: Long): List<WarehouseMovementContent> {
        val tempContent = runBlocking { dao.selectByTempIds(wmId) }

        val content: ArrayList<WarehouseMovementContent> = arrayListOf()
        tempContent.mapTo(content) { WarehouseMovementContent(it) }

        return content
    }


    fun insert(content: TempMovementContent) = runBlocking {
        dao.insert(content)
    }

    fun insert(wmId: Long, contents: List<WarehouseMovementContent>) = runBlocking {
        contents.forEach { it.warehouseMovementId = wmId }

        val tempContents: ArrayList<TempMovementContent> = arrayListOf()
        contents.mapTo(tempContents) {
            TempMovementContent(it)
        }

        tempContents.forEach {
            it.id = nextId
            dao.insert(it)
        }
    }

    fun deleteAll() = runBlocking {
        dao.deleteAll()
    }
}