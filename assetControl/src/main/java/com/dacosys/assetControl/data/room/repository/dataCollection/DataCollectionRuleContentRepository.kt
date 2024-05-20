package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent
import kotlinx.coroutines.runBlocking

class DataCollectionRuleContentRepository {
    private val dao: DataCollectionRuleContentDao
        get() = database.dataCollectionRuleContentDao()

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectAttributeCompositionIdByRouteId(routeId: Long) = runBlocking {
        dao.selectAttributeCompositionIdByRouteId(routeId)
    }

    fun selectByDataCollectionRuleIdActive(ruleId: Long) = runBlocking {
        dao.selectByDataCollectionRuleIdActive(ruleId)
    }


    fun insert(ruleContent: DataCollectionRuleContent) = runBlocking {
        dao.insert(ruleContent)
    }

    fun insert(contents: List<DataCollectionRuleContent>) = runBlocking {
        dao.insert(contents)
    }


    fun deleteByDataCollectionRuleId(ruleId: Long) = runBlocking {
        dao.deleteByDataCollectionRuleId(ruleId)
    }
}