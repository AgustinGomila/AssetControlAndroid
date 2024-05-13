package com.dacosys.assetControl.data.room.repository.location

import com.dacosys.assetControl.data.room.dao.location.WarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WarehouseAreaRepository {
    private val dao: WarehouseAreaDao by lazy {
        database.warehouseAreaDao()
    }

    val allWarehouseAreas: Flow<List<WarehouseArea>> = dao.getAllWarehouseAreas()

    suspend fun insertWarehouseArea(warehouseArea: WarehouseArea) {
        withContext(Dispatchers.IO) {
            dao.insertWarehouseArea(warehouseArea)
        }
    }

    suspend fun insertAllWarehouseAreas(warehouseAreas: List<WarehouseArea>) {
        withContext(Dispatchers.IO) {
            dao.insertAllWarehouseAreas(warehouseAreas)
        }
    }

    suspend fun updateWarehouseArea(warehouseArea: WarehouseArea) {
        withContext(Dispatchers.IO) {
            dao.updateWarehouseArea(warehouseArea)
        }
    }

    suspend fun deleteWarehouseArea(warehouseArea: WarehouseArea) {
        withContext(Dispatchers.IO) {
            dao.deleteWarehouseArea(warehouseArea)
        }
    }

    suspend fun deleteAllWarehouseAreas() {
        withContext(Dispatchers.IO) {
            dao.deleteAllWarehouseAreas()
        }
    }

    // Otros métodos del repositorio según sea necesario
}