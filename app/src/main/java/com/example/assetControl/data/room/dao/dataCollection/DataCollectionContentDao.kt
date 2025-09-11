package com.example.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.dto.attribute.AttributeCompositionEntry
import com.example.assetControl.data.room.dto.attribute.AttributeEntry
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContentEntry
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionEntry
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleContentEntry
import com.example.assetControl.data.room.entity.dataCollection.DataCollectionContentEntity

@Dao
interface DataCollectionContentDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} = :routeProcessId " +
                BASIC_ORDER
    )
    suspend fun selectByCollectorRouteProcessId(routeProcessId: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_ID} = :id " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionId(id: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ASSET_ID} = :assetId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdAssetId(
        ruleContentId: Long,
        assetId: Long
    ): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.WAREHOUSE_ID} = :wId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdWarehouseId(
        ruleContentId: Long,
        wId: Long
    ): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.WAREHOUSE_AREA_ID} = :waId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdWarehouseAreaId(
        ruleContentId: Long,
        waId: Long
    ): List<DataCollectionContent>

    @Query("SELECT MIN(${DataCollectionContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionContentEntity)


    @Query(
        "UPDATE ${DataCollectionContentEntry.TABLE_NAME} SET ${DataCollectionContentEntry.DATA_COLLECTION_ID} = :newValue " +
                "WHERE ${DataCollectionContentEntry.DATA_COLLECTION_ID} = :oldValue"
    )
    suspend fun updateDataCollectionId(newValue: Long, oldValue: Long)


    @Query("DELETE FROM ${DataCollectionContentEntry.TABLE_NAME} WHERE ${DataCollectionContentEntry.DATA_COLLECTION_ID} = :id")
    suspend fun deleteByDataCollectionId(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_ID} NOT IN ( " +
                "SELECT ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ID} FROM ${DataCollectionEntry.TABLE_NAME})"
    )
    suspend fun deleteOrphans()


    companion object {
        const val BASIC_SELECT = "SELECT ${DataCollectionContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${DataCollectionContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_ID}, " +
                    "${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.LEVEL}, " +
                    "${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.POSITION}"

        private const val BASIC_JOIN_FIELDS =
            "${AttributeCompositionEntry.TABLE_NAME}.${AttributeCompositionEntry.DESCRIPTION} AS ${DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_STR}," +
                    "${AttributeEntry.TABLE_NAME}.${AttributeEntry.DESCRIPTION} AS ${DataCollectionContentEntry.ATTRIBUTE_STR}," +
                    "${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.DESCRIPTION} AS ${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_STR}, " +
                    "${AttributeCompositionEntry.TABLE_NAME}.${AttributeCompositionEntry.ATTRIBUTE_COMPOSITION_TYPE_ID} AS ${DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_TYPE_ID} "

        private const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${DataCollectionEntry.TABLE_NAME} ON ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.DATA_COLLECTION_ID} = ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_ID} " +
                    "LEFT JOIN ${AttributeEntry.TABLE_NAME} ON ${AttributeEntry.TABLE_NAME}.${AttributeEntry.ID} = ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.ATTRIBUTE_ID} " +
                    "LEFT JOIN ${AttributeCompositionEntry.TABLE_NAME} ON ${AttributeCompositionEntry.TABLE_NAME}.${AttributeCompositionEntry.ID} = ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_ID} " +
                    "LEFT JOIN ${DataCollectionRuleContentEntry.TABLE_NAME} ON ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ID} = ${DataCollectionContentEntry.TABLE_NAME}.${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID} "
    }
}