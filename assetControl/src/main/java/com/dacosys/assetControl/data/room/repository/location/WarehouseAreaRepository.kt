package com.dacosys.assetControl.data.room.repository.location

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.location.WarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class WarehouseAreaRepository {
    private val dao: WarehouseAreaDao
        get() = database.warehouseAreaDao()

    val getAll: List<WarehouseArea> = runBlocking { dao.select() }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectNoTransferred() = runBlocking { ArrayList(dao.selectNoTransferred()) }

    fun select(onlyActive: Boolean): List<WarehouseArea> = runBlocking {
        if (onlyActive) dao.selectActive()
        else dao.select()
    }

    fun selectByDescription(wDescription: String, waDescription: String, onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectByDescriptionActive(wDescription, waDescription)
        else dao.selectByDescription(wDescription, waDescription)
    }

    fun selectByTempIds(): List<WarehouseArea> = runBlocking {
        val tempAreas = TempWarehouseAreaRepository().select()
        val ids = tempAreas.map { it.tempId }.toList()
        dao.selectByTempIds(ids)
    }

    val minId get() = runBlocking { dao.selectMinId() ?: -1 }

    fun insert(warehouseArea: WarehouseArea) = runBlocking {
        dao.insert(warehouseArea)
    }

    fun update(warehouseArea: WarehouseAreaObject) {
        val wa = WarehouseArea(warehouseArea)
        update(wa)
    }

    fun update(warehouseArea: WarehouseArea): Boolean {
        val r = runBlocking {
            dao.update(warehouseArea)
            true
        }
        return r
    }

    fun updateTransferred(ids: List<Long>): Boolean {
        val r = runBlocking {
            dao.updateTransferred(ids.toTypedArray())
            true
        }
        return r
    }

    fun updateWarehouseId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateWarehouseId(newValue, oldValue)
        }
    }

    suspend fun sync(
        areaObjects: Array<WarehouseAreaObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.WarehouseArea

        val areas: ArrayList<WarehouseArea> = arrayListOf()
        areaObjects.mapTo(areas) { WarehouseArea(it) }
        val partial = areas.count()

        withContext(Dispatchers.IO) {
            dao.insert(areas) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = getContext().getString(R.string.synchronizing_warehouse_areas),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }

    fun updateWarehouseAreaId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateWarehouseAreaId(newValue, oldValue)
        }
    }
}