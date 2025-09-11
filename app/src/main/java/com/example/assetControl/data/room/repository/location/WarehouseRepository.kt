package com.example.assetControl.data.room.repository.location

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.location.WarehouseDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.entity.location.WarehouseEntity
import com.example.assetControl.data.webservice.location.WarehouseObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class WarehouseRepository {
    private val dao: WarehouseDao
        get() = database.warehouseDao()

    fun select(onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectActive()
        else dao.select()
    }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectNoTransferred() = runBlocking { ArrayList(dao.selectNoTransferred()) }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(warehouse: Warehouse) = runBlocking {
        warehouse.id = nextLastId
        warehouse.transferred = 0
        dao.insert(WarehouseEntity(warehouse))
    }


    fun update(warehouse: Warehouse) = runBlocking {
        warehouse.transferred = 0
        dao.update(WarehouseEntity(warehouse))
    }

    fun updateWarehouseId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateId(newValue, oldValue)
    }

    fun updateTransferred(ids: List<Long>): Boolean {
        val r = runBlocking {
            dao.updateTransferred(ids.toTypedArray())
            true
        }
        return r
    }

    suspend fun sync(
        wObj: Array<WarehouseObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.Warehouse

        val warehouses: ArrayList<Warehouse> = arrayListOf()
        wObj.mapTo(warehouses) { Warehouse(it) }
        val partial = warehouses.count()

        withContext(Dispatchers.IO) {
            dao.insert(warehouses) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_assets),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}