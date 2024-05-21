package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleTargetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTargetEntity
import kotlinx.coroutines.runBlocking

class DataCollectionRuleTargetRepository {
    private val dao: DataCollectionRuleTargetDao
        get() = database.dataCollectionRuleTargetDao()

    fun insert(ruleContent: DataCollectionRuleTarget) = runBlocking {
        dao.insert(DataCollectionRuleTargetEntity(ruleContent))
    }

    fun insert(contents: List<DataCollectionRuleTarget>) = runBlocking {
        dao.insert(contents.map { DataCollectionRuleTargetEntity(it) })
    }


    fun deleteByDataCollectionRuleId(ruleId: Long) = runBlocking {
        dao.deleteByDataCollectionRuleId(ruleId)
    }
}