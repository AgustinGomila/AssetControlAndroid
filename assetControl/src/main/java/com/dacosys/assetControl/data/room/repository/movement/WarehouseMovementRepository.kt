package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarehouseMovementRepository {
    private val dao: WarehouseMovementDao by lazy {
        database.warehouseMovementDao()
    }

    suspend fun insert(warehouseMovement: WarehouseMovement) {
        withContext(Dispatchers.IO) {
            dao.insert(warehouseMovement)
        }
    }

    suspend fun update(warehouseMovement: WarehouseMovement) {
        withContext(Dispatchers.IO) {
            dao.update(warehouseMovement)
        }
    }

    suspend fun delete(warehouseMovement: WarehouseMovement) {
        withContext(Dispatchers.IO) {
            dao.delete(warehouseMovement)
        }
    }

    fun getWarehouseMovementById(id: Long): WarehouseMovement? {
        return dao.getWarehouseMovementById(id)
    }
}
