package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionEntity
import com.dacosys.assetControl.utils.misc.UTCDataTime.Companion.getUTCDateTimeAsNotNullDate
import kotlinx.coroutines.runBlocking
import java.util.*

class DataCollectionRepository {
    private val dao: DataCollectionDao
        get() = database.dataCollectionDao()

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByNoTransferred() = runBlocking { dao.selectByNoTransferred() }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(rpc: RouteProcessContent, dateStart: Date, routeProcessId: Long): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val assetId = rpc.assetId ?: 0L
            val warehouseAreaId = rpc.warehouseAreaId ?: 0L
            val warehouseId = rpc.warehouseId ?: 0L
            val nextId = nextLastId

            val d = DataCollection(
                id = nextId,
                assetId = assetId,
                warehouseId = warehouseId,
                warehouseAreaId = warehouseAreaId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = getUTCDateTimeAsNotNullDate(),
                completed = 1,
                routeProcessId = routeProcessId
            )
            dao.insert(DataCollectionEntity(d))
            dao.selectById(nextId)
        }
        return r
    }

    fun insert(asset: Asset, dateStart: Date): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val assetId = asset.id
            val nextId = nextLastId

            val d = DataCollection(
                id = nextId,
                assetId = assetId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = getUTCDateTimeAsNotNullDate(),
                completed = 1
            )
            dao.insert(DataCollectionEntity(d))
            dao.selectById(nextId)
        }
        return r
    }

    fun insert(warehouseArea: WarehouseArea, dateStart: Date): DataCollection? {
        val userId = getUserId() ?: return null

        val r = runBlocking {
            val warehouseAreaId = warehouseArea.id
            val warehouseId = warehouseArea.warehouseId
            val nextId = nextLastId

            val d = DataCollection(
                id = nextId,
                warehouseId = warehouseId,
                warehouseAreaId = warehouseAreaId,
                userId = userId,
                dateStart = dateStart,
                dateEnd = getUTCDateTimeAsNotNullDate(),
                completed = 1
            )
            dao.insert(DataCollectionEntity(d))
            dao.selectById(nextId)
        }
        return r
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

    fun deleteOrphansTransferred() {
        deleteOrphans()
        DataCollectionContentRepository().deleteOrphans()
    }

    private fun deleteOrphans() = runBlocking {
        dao.deleteOrphans()
    }
}