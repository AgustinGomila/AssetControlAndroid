package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataCollectionRuleContentDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllDataCollectionRuleContents(): Flow<List<DataCollectionRuleContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataCollectionRuleContent(dataCollectionRuleContent: DataCollectionRuleContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataCollectionRuleContents: List<DataCollectionRuleContent>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}