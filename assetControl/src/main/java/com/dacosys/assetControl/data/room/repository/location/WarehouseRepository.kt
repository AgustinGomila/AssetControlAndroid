package com.dacosys.assetControl.data.room.repository.location

import com.dacosys.assetControl.data.model.location.Warehouse
import com.dacosys.assetControl.data.room.dao.location.WarehouseDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WarehouseRepository {
    private val dao: WarehouseDao by lazy {
        database.warehouseDao()
    }

    val allWarehouses: Flow<List<Warehouse>> = dao.getAllWarehouses()

    suspend fun insertWarehouse(warehouse: Warehouse) {
        withContext(Dispatchers.IO) {
            dao.insertWarehouse(warehouse)
        }
    }

    suspend fun insertAllWarehouses(warehouses: List<Warehouse>) {
        withContext(Dispatchers.IO) {
            dao.insertAllWarehouses(warehouses)
        }
    }

    suspend fun updateWarehouse(warehouse: Warehouse) {
        withContext(Dispatchers.IO) {
            dao.updateWarehouse(warehouse)
        }
    }

    suspend fun deleteWarehouse(warehouse: Warehouse) {
        withContext(Dispatchers.IO) {
            dao.deleteWarehouse(warehouse)
        }
    }

    suspend fun deleteAllWarehouses() {
        withContext(Dispatchers.IO) {
            dao.deleteAllWarehouses()
        }
    }

    // Otros métodos del repositorio según sea necesario
}