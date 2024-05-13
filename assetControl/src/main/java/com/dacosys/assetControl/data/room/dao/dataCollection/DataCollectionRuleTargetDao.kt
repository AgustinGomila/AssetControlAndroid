package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataCollectionRuleTargetDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllDataCollectionRuleTargets(): Flow<List<DataCollectionRuleTarget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataCollectionRuleTarget(dataCollectionRuleTarget: DataCollectionRuleTarget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataCollectionRuleTargets: List<DataCollectionRuleTarget>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}