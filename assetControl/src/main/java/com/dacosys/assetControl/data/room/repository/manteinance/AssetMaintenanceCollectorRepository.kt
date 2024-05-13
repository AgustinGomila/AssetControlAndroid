package com.dacosys.assetControl.data.room.repository.manteinance

import com.dacosys.assetControl.data.room.dao.manteinance.AssetMaintenanceCollectorDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.manteinance.AssetMaintenanceCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetMaintenanceCollectorRepository {
    private val dao: AssetMaintenanceCollectorDao by lazy {
        database.assetMaintenanceCollectorDao()
    }

    fun getAllAssetMaintenanceCollectors() = dao.getAllAssetMaintenanceCollectors()

    fun getByAssetId(assetId: Long) = dao.getByAssetId(assetId)

    fun getById(assetId: Long, id: Long) = dao.getById(assetId, id)

    suspend fun insertAssetMaintenanceCollector(assetMaintenanceCollector: AssetMaintenanceCollector) {
        withContext(Dispatchers.IO) {
            dao.insertAssetMaintenanceCollector(assetMaintenanceCollector)
        }
    }

    suspend fun insertAll(assetMaintenanceCollectors: List<AssetMaintenanceCollector>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(assetMaintenanceCollectors)
        }
    }

    suspend fun updateAssetMaintenanceCollector(assetMaintenanceCollector: AssetMaintenanceCollector) {
        withContext(Dispatchers.IO) {
            dao.updateAssetMaintenanceCollector(assetMaintenanceCollector)
        }
    }

    suspend fun deleteById(assetId: Long, id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(assetId, id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
