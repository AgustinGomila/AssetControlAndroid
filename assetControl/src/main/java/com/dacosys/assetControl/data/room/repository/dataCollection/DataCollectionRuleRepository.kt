package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DataCollectionRuleRepository {
    private val dao: DataCollectionRuleDao by lazy {
        database.dataCollectionRuleDao()
    }

    fun getAllDataCollectionRules(): Flow<List<DataCollectionRule>> = dao.getAllDataCollectionRules()

    suspend fun insertDataCollectionRule(dataCollectionRule: DataCollectionRule) {
        withContext(Dispatchers.IO) {
            dao.insertDataCollectionRule(dataCollectionRule)
        }
    }

    suspend fun insertAll(dataCollectionRules: List<DataCollectionRule>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(dataCollectionRules)
        }
    }

    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}