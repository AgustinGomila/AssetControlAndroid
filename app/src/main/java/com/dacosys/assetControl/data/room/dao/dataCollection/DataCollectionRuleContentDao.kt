package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.attribute.AttributeCompositionEntry
import com.dacosys.assetControl.data.room.dto.attribute.AttributeEntry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleContentEntry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleEntry
import com.dacosys.assetControl.data.room.dto.route.RouteCompositionEntry
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContentEntity


@Dao
interface DataCollectionRuleContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<DataCollectionRuleContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<DataCollectionRuleContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): DataCollectionRuleContent?

    @Query(
        "SELECT ${DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_ID} $BASIC_FROM $ATTR_COMP_BASIC_JOIN_FIELDS " +
                "WHERE ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.ROUTE_ID} = :routeId " +
                "AND ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_ID} IS NOT NULL " +
                "AND ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_ID} > 0"
    )
    suspend fun selectAttributeCompositionIdByRouteId(routeId: Long): List<Long>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_ID} = :ruleId " +
                "AND ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ACTIVE} = 1"
    )
    suspend fun selectByDataCollectionRuleIdActive(ruleId: Long): List<DataCollectionRuleContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionRuleContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categories: List<DataCollectionRuleContentEntity>)

    @Transaction
    suspend fun insert(entities: List<DataCollectionRuleContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(DataCollectionRuleContentEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(asset: DataCollectionRuleContentEntity)


    @Delete
    suspend fun delete(content: DataCollectionRuleContentEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_ID} = :ruleId")
    suspend fun deleteByDataCollectionRuleId(ruleId: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${DataCollectionRuleContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${DataCollectionRuleContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_ID}, " +
                    "${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.LEVEL}, " +
                    "${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.POSITION}"


        const val BASIC_JOIN_FIELDS =
            "${AttributeEntry.TABLE_NAME}.${AttributeEntry.DESCRIPTION} AS ${DataCollectionRuleContentEntry.ATTRIBUTE_STR}, " +
                    "${AttributeCompositionEntry.TABLE_NAME}.${AttributeCompositionEntry.DESCRIPTION} AS ${DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_STR}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${AttributeEntry.TABLE_NAME} ON ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ATTRIBUTE_ID} = ${AttributeEntry.TABLE_NAME}.${AttributeEntry.ID} " +
                    "LEFT JOIN ${AttributeCompositionEntry.TABLE_NAME} ON ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.ATTRIBUTE_COMPOSITION_ID} = ${AttributeCompositionEntry.TABLE_NAME}.${AttributeCompositionEntry.ID}"

        const val ATTR_COMP_BASIC_JOIN_FIELDS =
            "LEFT JOIN ${DataCollectionRuleEntry.TABLE_NAME} ON ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ID} = ${DataCollectionRuleContentEntry.TABLE_NAME}.${DataCollectionRuleContentEntry.DATA_COLLECTION_RULE_ID} " +
                    "LEFT JOIN ${RouteCompositionEntry.TABLE_NAME} ON ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.DATA_COLLECTION_RULE_ID} = ${DataCollectionRuleEntry.TABLE_NAME}.${DataCollectionRuleEntry.ID}"
    }
}