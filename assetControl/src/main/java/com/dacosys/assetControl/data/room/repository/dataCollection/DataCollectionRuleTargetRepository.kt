package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleTargetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataCollectionRuleTargetRepository {
    private val dao: DataCollectionRuleTargetDao by lazy {
        database.dataCollectionRuleTargetDao()
    }

    suspend fun insertDataCollectionRuleTarget(dataCollectionRuleTarget: DataCollectionRuleTarget) {
        withContext(Dispatchers.IO) {
            dao.insertDataCollectionRuleTarget(dataCollectionRuleTarget)
        }
    }

    suspend fun insertAll(dataCollectionRuleTargets: List<DataCollectionRuleTarget>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(dataCollectionRuleTargets)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}