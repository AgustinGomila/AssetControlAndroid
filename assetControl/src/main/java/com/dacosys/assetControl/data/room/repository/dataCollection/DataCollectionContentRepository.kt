package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DataCollectionContentRepository {
    private val dao: DataCollectionContentDao by lazy {
        database.dataCollectionContentDao()
    }

    fun getAllDataCollectionContents(): Flow<List<DataCollectionContent>> = dao.getAllDataCollectionContents()

    suspend fun insertDataCollectionContent(dataCollectionContent: DataCollectionContent) {
        withContext(Dispatchers.IO) {
            dao.insertDataCollectionContent(dataCollectionContent)
        }
    }

    suspend fun insertAll(dataCollectionContents: List<DataCollectionContent>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(dataCollectionContents)
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