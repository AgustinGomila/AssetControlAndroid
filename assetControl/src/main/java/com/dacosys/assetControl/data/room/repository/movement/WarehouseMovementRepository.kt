package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementEntity
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.getUTCDateTimeAsNotNullDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class WarehouseMovementRepository {
    private val dao: WarehouseMovementDao
        get() = database.warehouseMovementDao()

    fun selectByNoTransferred() = runBlocking { ArrayList(dao.selectByNoTransferred()) }


    suspend fun insert(warehouseMovement: WarehouseMovement): Long =
        withContext(Dispatchers.IO) {
            dao.insert(WarehouseMovementEntity(warehouseMovement))
        }


    fun update(warehouseMovement: WarehouseMovement) {
        runBlocking(Dispatchers.IO) {
            dao.update(WarehouseMovementEntity(warehouseMovement))
        }
    }

    fun updateOriginWarehouseId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateOriginWarehouseId(newValue, oldValue)
        }
    }

    fun updateDestinationWarehouseId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateDestinationWarehouseId(newValue, oldValue)
        }
    }

    fun updateOriginWarehouseAreaId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateOriginWarehouseAreaId(newValue, oldValue)
        }
    }

    fun updateDestinationWarehouseAreaId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateDestinationWarehouseAreaId(newValue, oldValue)
        }
    }

    fun updateTransferred(newValue: Long, oldValue: Long) {
        runBlocking {
            val date = getUTCDateTimeAsNotNullDate()
            dao.updateId(newValue, oldValue, date)
        }
    }


    fun deleteById(id: Long) = runBlocking {
        dao.deleteById(id)
    }

    fun deleteTransferred() = runBlocking {
        WarehouseMovementContentRepository().deleteTransferred()
        dao.deleteTransferred()
    }
}
