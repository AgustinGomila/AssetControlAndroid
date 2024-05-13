package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataCollectionDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllDataCollections(): Flow<List<DataCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataCollection(dataCollection: DataCollection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataCollections: List<DataCollection>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}