package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.data.room.dao.movement.TempMovementContentDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContentEntity
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


    fun insert(content: TempMovementContentEntity) = runBlocking {
        dao.insert(content)
    }

    fun insert(wmId: Long, contents: List<WarehouseMovementContent>) = runBlocking {
        contents.forEach { it.warehouseMovementId = wmId }

        val tempContents: ArrayList<TempMovementContentEntity> = arrayListOf()
        contents.mapTo(tempContents) {
            TempMovementContentEntity(it)
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