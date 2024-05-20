package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.attribute.Attribute
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent.Entry
import com.dacosys.assetControl.data.room.entity.route.RouteComposition


@Dao
interface DataCollectionRuleContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<DataCollectionRuleContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<DataCollectionRuleContent>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): DataCollectionRuleContent?

    @Query(
        "SELECT ${Entry.ATTRIBUTE_COMPOSITION_ID} $BASIC_FROM $ATTR_COMP_BASIC_JOIN_FIELDS " +
                "WHERE ${routeCompEntry.TABLE_NAME}.${routeCompEntry.ROUTE_ID} = :routeId " +
                "AND ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID} IS NOT NULL " +
                "AND ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID} > 0"
    )
    suspend fun selectAttributeCompositionIdByRouteId(routeId: Long): List<Long>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $ATTR_COMP_BASIC_JOIN_FIELDS " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_ID} = :ruleId " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1"
    )
    suspend fun selectByDataCollectionRuleIdActive(ruleId: Long): List<DataCollectionRuleContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionRuleContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categories: List<DataCollectionRuleContent>)

    @Transaction
    suspend fun insert(entities: List<DataCollectionRuleContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(asset: DataCollectionRuleContent)


    @Delete
    suspend fun delete(content: DataCollectionRuleContent)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${Entry.DATA_COLLECTION_RULE_ID} = :ruleId")
    suspend fun deleteByDataCollectionRuleId(ruleId: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.LEVEL}, " +
                "${Entry.TABLE_NAME}.${Entry.POSITION}"

        private val attributeEntry = Attribute.Entry
        private val dataColRuleEntry = DataCollectionRule.Entry
        private val routeCompEntry = RouteComposition.Entry

        const val BASIC_JOIN_FIELDS =
            "${attributeEntry.TABLE_NAME}.${attributeEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_STR}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${attributeEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_ID} = ${attributeEntry.TABLE_NAME}.${attributeEntry.ID}"

        const val ATTR_COMP_BASIC_JOIN_FIELDS =
            "LEFT JOIN ${dataColRuleEntry.TABLE_NAME} ON ${dataColRuleEntry.TABLE_NAME}.${dataColRuleEntry.ID} = ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_ID} " +
                    "LEFT JOIN ${routeCompEntry.TABLE_NAME} ON ${routeCompEntry.TABLE_NAME}.${routeCompEntry.DATA_COLLECTION_RULE_ID} = ${dataColRuleEntry.TABLE_NAME}.${dataColRuleEntry.ID}"
    }
}