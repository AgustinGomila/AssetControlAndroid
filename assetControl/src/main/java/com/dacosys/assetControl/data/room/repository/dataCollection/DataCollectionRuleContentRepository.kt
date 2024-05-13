package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DataCollectionRuleContentRepository {
    private val dao: DataCollectionRuleContentDao by lazy {
        database.dataCollectionRuleContentDao()
    }

    fun getAllDataCollectionRuleContents(): Flow<List<DataCollectionRuleContent>> =
        dao.getAllDataCollectionRuleContents()

    suspend fun insertDataCollectionRuleContent(dataCollectionRuleContent: DataCollectionRuleContent) {
        withContext(Dispatchers.IO) {
            dao.insertDataCollectionRuleContent(dataCollectionRuleContent)
        }
    }

    suspend fun insertAll(dataCollectionRuleContents: List<DataCollectionRuleContent>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(dataCollectionRuleContents)
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