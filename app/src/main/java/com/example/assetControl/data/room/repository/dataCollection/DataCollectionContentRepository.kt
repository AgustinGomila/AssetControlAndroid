package com.example.assetControl.data.room.repository.dataCollection

import com.example.assetControl.data.room.dao.dataCollection.DataCollectionContentDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.example.assetControl.data.room.entity.dataCollection.DataCollectionContentEntity
import kotlinx.coroutines.runBlocking

class DataCollectionContentRepository {
    private val dao: DataCollectionContentDao
        get() = database.dataCollectionContentDao()

    fun selectByDataCollectionId(id: Long) = runBlocking { dao.selectByDataCollectionId(id) }

    fun selectByCollectorRouteProcessId(routeProcessId: Long) = runBlocking {
        dao.selectByCollectorRouteProcessId(routeProcessId)
    }

    fun selectByDataCollectionRuleContentIdAssetId(dcrContId: Long, assetId: Long) = runBlocking {
        dao.selectByDataCollectionRuleContentIdAssetId(dcrContId, assetId)
    }

    fun selectByDataCollectionRuleContentIdWarehouseId(dcrContId: Long, wId: Long) = runBlocking {
        dao.selectByDataCollectionRuleContentIdWarehouseId(dcrContId, wId)
    }

    fun selectByDataCollectionRuleContentIdWarehouseAreaId(dcrContId: Long, waId: Long) = runBlocking {
        dao.selectByDataCollectionRuleContentIdWarehouseAreaId(dcrContId, waId)
    }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(id: Long, dcc: DataCollectionContent): Boolean {
        val r = runBlocking {
            val nextId = nextLastId

            dcc.dataCollectionId = id
            dcc.id = nextId

            dao.insert(DataCollectionContentEntity(dcc))
            true
        }
        return r
    }


    fun updateDataCollectionId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateDataCollectionId(newValue, oldValue)
        }
    }


    fun deleteByDataCollectionId(dccId: Long) = runBlocking {
        dao.deleteByDataCollectionId(dccId)
    }

    fun deleteOrphans() = runBlocking {
        dao.deleteOrphans()
    }
}