package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent
import kotlinx.coroutines.runBlocking

class DataCollectionContentRepository {
    private val dao: DataCollectionContentDao
        get() = database.dataCollectionContentDao()

    fun selectByDataCollectionId(id: Long) = dao.selectByDataCollectionId(id)

    fun selectByCollectorRouteProcessId(routeProcessId: Long) = dao.selectByCollectorRouteProcessId(routeProcessId)

    fun selectByDataCollectionRuleContentIdAssetId(dcrContId: Long, assetId: Long) =
        dao.selectByDataCollectionRuleContentIdAssetId(dcrContId, assetId)

    fun selectByDataCollectionRuleContentIdWarehouseId(dcrContId: Long, wId: Long) =
        dao.selectByDataCollectionRuleContentIdWarehouseId(dcrContId, wId)

    fun selectByDataCollectionRuleContentIdWarehouseAreaId(dcrContId: Long, waId: Long) =
        dao.selectByDataCollectionRuleContentIdWarehouseAreaId(dcrContId, waId)

    fun insert(id: Long, dcc: DataCollectionContent): Boolean {
        val r = runBlocking {
            dcc.dataCollectionId = id
            dao.insert(dcc)
            true
        }
        return r
    }

    fun insert(id: Long, dccList: List<DataCollectionContent>): Boolean {
        val r = runBlocking {
            dccList.forEach { it.dataCollectionId = id }
            dao.insert(dccList)
            true
        }
        return r
    }


    fun deleteByDataCollectionId(dccId: Long) = runBlocking {
        dao.deleteByDataCollectionId(dccId)
    }

    fun deleteOrphans() = runBlocking {
        dao.deleteOrphans()
    }
}