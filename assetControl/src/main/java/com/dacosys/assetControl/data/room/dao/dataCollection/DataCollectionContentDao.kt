package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataCollectionContentDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllDataCollectionContents(): Flow<List<DataCollectionContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataCollectionContent(dataCollectionContent: DataCollectionContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataCollectionContents: List<DataCollectionContent>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}