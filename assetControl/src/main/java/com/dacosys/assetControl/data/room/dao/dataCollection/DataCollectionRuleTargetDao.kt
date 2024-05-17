package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget.Entry

@Dao
interface DataCollectionRuleTargetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(target: DataCollectionRuleTarget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(targets: List<DataCollectionRuleTarget>)


    @Query("DELETE $BASIC_FROM WHERE ${Entry.DATA_COLLECTION_RULE_ID} = :ruleId")
    suspend fun deleteByDataCollectionRuleId(ruleId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}