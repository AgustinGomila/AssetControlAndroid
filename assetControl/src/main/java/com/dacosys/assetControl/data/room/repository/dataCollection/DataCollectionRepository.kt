package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent
import kotlinx.coroutines.runBlocking
import java.util.*

class DataCollectionRepository {
    private val dao: DataCollectionDao
        get() = database.dataCollectionDao()

    fun selectById(id: Long) = runBlocking { dao.selectByCollectorId(id) }

    fun selectByNoTransferred() = runBlocking { dao.selectByNoTransferred() }

    private val nextId: Long
        get() = runBlocking { (dao.selectMaxId() ?: 0) + 1 }


    fun insert(rpc: RouteProcessContent, dateStart: Date, routeProcessId: Long): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val assetId = rpc.assetId ?: 0L
            val warehouseAreaId = rpc.warehouseAreaId ?: 0L
            val warehouseId = rpc.warehouseId ?: 0L
            val newId = nextId

            val d = DataCollection(
                id = newId,
                assetId = assetId,
                warehouseId = warehouseId,
                warehouseAreaId = warehouseAreaId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = Date(),
                completed = 1,
                collectorRouteProcessId = routeProcessId
            )
            dao.insert(d)
            return@runBlocking d
        }
        return r
    }

    fun insert(asset: Asset, dateStart: Date, routeProcessId: Long): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val assetId = asset.id
            val newId = nextId

            val d = DataCollection(
                id = newId,
                assetId = assetId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = Date(),
                completed = 1,
                collectorRouteProcessId = routeProcessId
            )
            dao.insert(d)
            return@runBlocking d
        }
        return r
    }

    fun insert(warehouseArea: WarehouseArea, dateStart: Date, routeProcessId: Long): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val warehouseAreaId = warehouseArea.id
            val warehouseId = warehouseArea.warehouseId
            val newId = nextId

            val d = DataCollection(
                id = newId,
                warehouseId = warehouseId,
                warehouseAreaId = warehouseAreaId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = Date(),
                completed = 1,
                collectorRouteProcessId = routeProcessId
            )
            dao.insert(d)
            return@runBlocking d
        }
        return r
    }


    fun updateTransferredNew(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateId(newValue, oldValue)
        }
    }


    fun deleteById(id: Long) = runBlocking {
        dao.deleteById(id)
    }

    fun deleteOrphansTransferred() {
        deleteOrphans()
        DataCollectionContentRepository().deleteOrphans()
    }

    private fun deleteOrphans() = runBlocking {
        dao.deleteOrphans()
    }
}