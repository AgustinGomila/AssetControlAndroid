package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.attribute.Attribute
import com.dacosys.assetControl.data.room.dto.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionContent.Entry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContentEntity

@Dao
interface DataCollectionContentDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${dcEntry.TABLE_NAME}.${dcEntry.ROUTE_PROCESS_ID} = :routeProcessId " +
                BASIC_ORDER
    )
    suspend fun selectByCollectorRouteProcessId(routeProcessId: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} = :id " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionId(id: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.ASSET_ID} = :assetId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdAssetId(
        ruleContentId: Long,
        assetId: Long
    ): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.WAREHOUSE_ID} = :wId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdWarehouseId(
        ruleContentId: Long,
        wId: Long
    ): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.WAREHOUSE_AREA_ID} = :waId " +
                BASIC_ORDER
    )
    suspend fun selectByDataCollectionRuleContentIdWarehouseAreaId(
        ruleContentId: Long,
        waId: Long
    ): List<DataCollectionContent>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionContentEntity)


    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.DATA_COLLECTION_ID} = :newValue " +
                "WHERE ${Entry.DATA_COLLECTION_ID} = :oldValue"
    )
    suspend fun updateDataCollectionId(newValue: Long, oldValue: Long)


    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.DATA_COLLECTION_ID} = :id")
    suspend fun deleteByDataCollectionId(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} NOT IN ( " +
                "SELECT ${dcEntry.TABLE_NAME}.${dcEntry.ID} FROM ${dcEntry.TABLE_NAME})"
    )
    suspend fun deleteOrphans()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.LEVEL}, " +
                "${Entry.TABLE_NAME}.${Entry.POSITION}"

        private val rcEntry = DataCollectionRuleContent.Entry
        private val acEntry = AttributeComposition.Entry
        private val aEntry = Attribute.Entry
        private val dcEntry = DataCollection.Entry

        private const val BASIC_JOIN_FIELDS =
            "${acEntry.TABLE_NAME}.${acEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_COMPOSITION_STR}," +
                    "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_STR}," +
                    "${rcEntry.TABLE_NAME}.${rcEntry.DESCRIPTION} AS ${Entry.DATA_COLLECTION_RULE_CONTENT_STR}, " +
                    "${acEntry.TABLE_NAME}.${acEntry.ATTRIBUTE_COMPOSITION_TYPE_ID} AS ${Entry.ATTRIBUTE_COMPOSITION_TYPE_ID} "

        private const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${dcEntry.TABLE_NAME} ON ${dcEntry.TABLE_NAME}.${dcEntry.DATA_COLLECTION_ID} = ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} " +
                    "LEFT JOIN ${aEntry.TABLE_NAME} ON ${aEntry.TABLE_NAME}.${aEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_ID} " +
                    "LEFT JOIN ${acEntry.TABLE_NAME} ON ${acEntry.TABLE_NAME}.${acEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID} " +
                    "LEFT JOIN ${rcEntry.TABLE_NAME} ON ${rcEntry.TABLE_NAME}.${rcEntry.ID} = ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} "
    }
}