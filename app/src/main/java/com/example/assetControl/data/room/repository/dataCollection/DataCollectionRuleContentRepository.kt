package com.example.assetControl.data.room.repository.dataCollection

import com.example.assetControl.data.room.dao.dataCollection.DataCollectionRuleContentDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.example.assetControl.data.room.entity.dataCollection.DataCollectionRuleContentEntity
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
        dao.insert(DataCollectionRuleContentEntity(ruleContent))
    }

    fun insert(contents: List<DataCollectionRuleContent>) = runBlocking {
        dao.insert(contents.map { DataCollectionRuleContentEntity(it) })
    }


    fun deleteByDataCollectionRuleId(ruleId: Long) = runBlocking {
        dao.deleteByDataCollectionRuleId(ruleId)
    }
}