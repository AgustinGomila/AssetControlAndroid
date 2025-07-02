package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleEntry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleTargetEntry
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleEntity

@Dao
interface DataCollectionRuleDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ID} = :id")
    suspend fun selectById(id: Long): DataCollectionRule?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER")
    suspend fun selectByDescription(description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.ASSET_ID} = :assetId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetAssetIdDescription(assetId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.ASSET_ID} = :assetId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectByTargetAssetIdDescriptionActive(assetId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID} = :waId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetWarehouseAreaIdDescription(waId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID} = :waId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectByTargetWarehouseAreaIdDescriptionActive(
        waId: Long,
        description: String
    ): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID} = :icId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetItemCategoryIdDescription(icId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID} = :icId " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectByTargetItemCategoryIdDescriptionActive(icId: Long, description: String): List<DataCollectionRule>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: DataCollectionRuleEntity)

    @Transaction
    suspend fun insert(entities: List<DataCollectionRule>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(DataCollectionRuleEntity(entity))
            completedTask(index + 1)
        }
    }


    companion object {
        const val BASIC_SELECT = "SELECT ${DataCollectionRuleEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${DataCollectionRuleEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.DESCRIPTION}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${DataCollectionRuleTargetEntry.TABLE_NAME} ON ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ID} = ${DataCollectionRuleTargetEntry.TABLE_NAME}.${DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID}"
    }
}