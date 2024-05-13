package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DataCollectionRepository {
    private val dao: DataCollectionDao by lazy {
        database.dataCollectionDao()
    }

    fun getAllDataCollections(): Flow<List<DataCollection>> = dao.getAllDataCollections()

    suspend fun insertDataCollection(dataCollection: DataCollection) {
        withContext(Dispatchers.IO) {
            dao.insertDataCollection(dataCollection)
        }
    }

    suspend fun insertAll(dataCollections: List<DataCollection>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(dataCollections)
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