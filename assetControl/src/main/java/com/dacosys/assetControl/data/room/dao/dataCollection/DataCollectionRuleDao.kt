package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule.Entry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleEntity

@Dao
interface DataCollectionRuleDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    suspend fun selectById(id: Long): DataCollectionRule?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER")
    suspend fun selectByDescription(description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.ASSET_ID} = :assetId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetAssetIdDescription(assetId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.ASSET_ID} = :assetId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectByTargetAssetIdDescriptionActive(assetId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.WAREHOUSE_AREA_ID} = :waId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetWarehouseAreaIdDescription(waId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.WAREHOUSE_AREA_ID} = :waId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectByTargetWarehouseAreaIdDescriptionActive(
        waId: Long,
        description: String
    ): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.ITEM_CATEGORY_ID} = :icId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  $BASIC_ORDER"
    )
    suspend fun selectByTargetItemCategoryIdDescription(icId: Long, description: String): List<DataCollectionRule>

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${tEntry.TABLE_NAME}.${tEntry.ITEM_CATEGORY_ID} = :icId " +
                "AND ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER"
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
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}"

        private val tEntry = DataCollectionRuleTarget.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${tEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.ID} = ${tEntry.TABLE_NAME}.${tEntry.DATA_COLLECTION_RULE_ID}"
    }
}