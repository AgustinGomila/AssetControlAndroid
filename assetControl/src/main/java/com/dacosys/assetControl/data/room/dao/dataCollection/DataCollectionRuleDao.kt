package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRule.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataCollectionRuleDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllDataCollectionRules(): Flow<List<DataCollectionRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataCollectionRule(dataCollectionRule: DataCollectionRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataCollectionRules: List<DataCollectionRule>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}