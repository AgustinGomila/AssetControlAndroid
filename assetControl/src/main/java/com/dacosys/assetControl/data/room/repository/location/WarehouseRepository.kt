package com.dacosys.assetControl.data.room.repository.location

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.location.WarehouseDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.webservice.location.WarehouseObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class WarehouseRepository {
    private val dao: WarehouseDao
        get() = database.warehouseDao()

    fun select(onlyActive: Boolean) =
        if (onlyActive) dao.selectActive() else dao.select()

    fun selectById(id: Long) = dao.selectById(id)

    fun selectNoTransferred() = ArrayList(dao.selectNoTransferred())

    val minId get() = dao.selectMinId() ?: -1


    fun insert(warehouse: Warehouse) = runBlocking {
        dao.insert(warehouse)
    }


    fun update(warehouse: Warehouse): Boolean {
        val r = runBlocking {
            dao.update(warehouse)
            true
        }
        return r
    }

    fun updateWarehouseId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateId(newValue, oldValue)
        }
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
                        msg = getContext().getString(R.string.synchronizing_assets),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}