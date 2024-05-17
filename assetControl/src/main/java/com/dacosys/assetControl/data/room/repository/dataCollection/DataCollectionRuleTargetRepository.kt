package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleTargetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget
import kotlinx.coroutines.runBlocking

class DataCollectionRuleTargetRepository {
    private val dao: DataCollectionRuleTargetDao
        get() = database.dataCollectionRuleTargetDao()

    fun insert(ruleContent: DataCollectionRuleTarget) = runBlocking {
        dao.insert(ruleContent)
    }

    fun insert(contents: List<DataCollectionRuleTarget>) = runBlocking {
        dao.insert(contents)
    }


    fun deleteByDataCollectionRuleId(ruleId: Long) = runBlocking {
        dao.deleteByDataCollectionRuleId(ruleId)
    }
}