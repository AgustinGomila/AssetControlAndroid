package com.example.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleTargetEntry
import com.example.assetControl.data.room.entity.dataCollection.DataCollectionRuleTargetEntity

@Dao
interface DataCollectionRuleTargetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(target: DataCollectionRuleTargetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(targets: List<DataCollectionRuleTargetEntity>)


    @Query("DELETE $BASIC_FROM WHERE ${DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID} = :ruleId")
    suspend fun deleteByDataCollectionRuleId(ruleId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${DataCollectionRuleTargetEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${DataCollectionRuleTargetEntry.TABLE_NAME}"
    }
}