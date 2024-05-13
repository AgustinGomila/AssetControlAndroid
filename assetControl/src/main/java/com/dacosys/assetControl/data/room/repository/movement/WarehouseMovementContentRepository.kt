package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WarehouseMovementContentRepository {
    private val dao: WarehouseMovementContentDao by lazy {
        database.warehouseMovementContentDao()
    }

    val allWarehouseMovementContents: Flow<List<WarehouseMovementContent>> = dao.getAllWarehouseMovementContents()

    suspend fun insert(content: WarehouseMovementContent) {
        withContext(Dispatchers.IO) {
            dao.insertWarehouseMovementContent(content)
        }
    }

    suspend fun update(content: WarehouseMovementContent) {
        withContext(Dispatchers.IO) {
            dao.updateWarehouseMovementContent(content)
        }
    }

    suspend fun delete(content: WarehouseMovementContent) {
        withContext(Dispatchers.IO) {
            dao.deleteWarehouseMovementContent(content)
        }
    }
}