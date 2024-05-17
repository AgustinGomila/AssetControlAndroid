package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent.Entry
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent

@Dao
interface DataCollectionContentDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $SPECIAL_LEFT_JOIN " +
                "WHERE ${dcEntry.TABLE_NAME}.${dcEntry.COLLECTOR_ROUTE_PROCESS_ID} = :routeProcessId " +
                BASIC_ORDER
    )
    fun selectByCollectorRouteProcessId(routeProcessId: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $SPECIAL_LEFT_JOIN" +
                "WHERE ${dcEntry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} = :id " +
                BASIC_ORDER
    )
    fun selectByDataCollectionId(id: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $SPECIAL_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.ASSET_ID} = :assetId " +
                BASIC_ORDER
    )
    fun selectByDataCollectionRuleContentIdAssetId(ruleContentId: Long, assetId: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $SPECIAL_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.WAREHOUSE_ID} = :wId " +
                BASIC_ORDER
    )
    fun selectByDataCollectionRuleContentIdWarehouseId(ruleContentId: Long, wId: Long): List<DataCollectionContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $SPECIAL_LEFT_JOIN" +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = :ruleContentId " +
                "AND ${dcEntry.TABLE_NAME}.${dcEntry.WAREHOUSE_AREA_ID} = :waId " +
                BASIC_ORDER
    )
    fun selectByDataCollectionRuleContentIdWarehouseAreaId(ruleContentId: Long, waId: Long): List<DataCollectionContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<DataCollectionContent>)


    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.DATA_COLLECTION_ID} = :id")
    suspend fun deleteByDataCollectionId(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} NOT IN ( " +
                "SELECT ${dcEntry.TABLE_NAME}.${dcEntry.DATA_COLLECTION_ID} FROM ${dcEntry.TABLE_NAME})"
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
        private val dcEntry = DataCollection.Entry

        const val BASIC_JOIN_FIELDS =
            "${rcEntry.TABLE_NAME}.${rcEntry.DESCRIPTION} AS ${Entry.DATA_COLLECTION_RULE_CONTENT_STR}, " +
                    "${acEntry.TABLE_NAME}.${acEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_COMPOSITION_STR}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${rcEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = ${rcEntry.TABLE_NAME}.${rcEntry.ID} " +
                    "LEFT JOIN ${acEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID} = ${acEntry.TABLE_NAME}.${acEntry.ID}"

        const val SPECIAL_LEFT_JOIN =
            "LEFT JOIN ${dcEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} = ${dcEntry.TABLE_NAME}.${dcEntry.DATA_COLLECTION_ID} "
    }
}