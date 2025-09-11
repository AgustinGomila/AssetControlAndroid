package com.example.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.assetControl.data.room.dto.asset.AssetEntry
import com.example.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.example.assetControl.data.room.dto.location.WarehouseEntry
import com.example.assetControl.data.room.dto.route.RouteCompositionEntry
import com.example.assetControl.data.room.dto.route.RouteEntry
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.data.room.dto.route.RouteProcessContentEntry
import com.example.assetControl.data.room.dto.route.RouteProcessEntry
import com.example.assetControl.data.room.entity.route.RouteProcessContentEntity

@Dao
interface RouteProcessContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<RouteProcessContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): RouteProcessContent?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.ROUTE_PROCESS_ID} = :id"
    )
    suspend fun selectByRouteProcessId(id: Long): List<RouteProcessContent>

    @Query("SELECT MIN(${RouteProcessContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: RouteProcessContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: List<RouteProcessContentEntity>): List<Long>


    @Update
    suspend fun update(content: RouteProcessContentEntity)

    @Query(
        "UPDATE ${RouteProcessContentEntry.TABLE_NAME} " +
                "SET ${RouteProcessContentEntry.ROUTE_PROCESS_STATUS_ID} = :processStatusId, ${RouteProcessContentEntry.DATA_COLLECTION_ID} = :dataCollectionId " +
                "WHERE ${RouteProcessContentEntry.ROUTE_PROCESS_ID} = :routeProcessId " +
                "AND ${RouteProcessContentEntry.ID} = :id " +
                "AND ${RouteProcessContentEntry.DATA_COLLECTION_RULE_ID} = :dataCollectionRuleId " +
                "AND ${RouteProcessContentEntry.LEVEL} = :level " +
                "AND ${RouteProcessContentEntry.POSITION} = :position "
    )
    suspend fun updateStatus(
        id: Long,
        processStatusId: Int,
        dataCollectionId: Long?,
        routeProcessId: Long,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
    )

    @Query(
        "UPDATE ${RouteProcessContentEntry.TABLE_NAME} SET ${RouteProcessContentEntry.ROUTE_PROCESS_ID} = :newValue " +
                "WHERE ${RouteProcessContentEntry.ROUTE_PROCESS_ID} = :oldValue"
    )
    suspend fun updateRouteProcessId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${RouteProcessContentEntry.TABLE_NAME} SET ${RouteProcessContentEntry.DATA_COLLECTION_ID} = :newValue " +
                "WHERE ${RouteProcessContentEntry.DATA_COLLECTION_ID} = :oldValue"
    )
    suspend fun updateDataCollectionId(newValue: Long, oldValue: Long)


    @Delete
    suspend fun delete(content: RouteProcessContentEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${RouteProcessContentEntry.ROUTE_PROCESS_ID} = :id")
    suspend fun deleteByRouteProcessId(id: Long)


    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${RouteProcessContentEntry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${RouteProcessEntry.ROUTE_PROCESS_ID} FROM ${RouteProcessEntry.TABLE_NAME} " +
                "WHERE ${RouteProcessEntry.ROUTE_PROCESS_DATE} < :routeProcessDate " +
                "AND ${RouteProcessEntry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${RouteProcessEntry.ROUTE_ID} = :routeId )"
    )
    suspend fun deleteByRouteIdRouteProcessDate(routeProcessDate: String, routeId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${RouteProcessContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${RouteProcessContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.ROUTE_PROCESS_ID}, " +
                    "${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.LEVEL}, " +
                    "${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.POSITION}"

        const val BASIC_JOIN_FIELDS =
            "${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.ASSET_ID} AS ${RouteProcessContentEntry.ASSET_ID}, " +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} AS ${RouteProcessContentEntry.ASSET_CODE}, " +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} AS ${RouteProcessContentEntry.ASSET_STR}, " +
                    "${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} AS ${RouteProcessContentEntry.ROUTE_ID}, " +
                    "${RouteEntry.TABLE_NAME}.${RouteEntry.DESCRIPTION} AS ${RouteProcessContentEntry.ROUTE_STR}, " +
                    "${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.WAREHOUSE_ID} AS ${RouteProcessContentEntry.WAREHOUSE_ID}, " +
                    "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${RouteProcessContentEntry.WAREHOUSE_STR}, " +
                    "${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.WAREHOUSE_AREA_ID} AS ${RouteProcessContentEntry.WAREHOUSE_AREA_ID}, " +
                    "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${RouteProcessContentEntry.WAREHOUSE_AREA_STR} "


        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${RouteProcessEntry.TABLE_NAME} ON ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ID} = ${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.ROUTE_PROCESS_ID} " +
                    "LEFT JOIN ${RouteCompositionEntry.TABLE_NAME} ON ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.ROUTE_ID} = ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} AND " +
                    "${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.DATA_COLLECTION_RULE_ID} = ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.DATA_COLLECTION_RULE_ID} AND " +
                    "${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.LEVEL} = ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.LEVEL} AND " +
                    "${RouteProcessContentEntry.TABLE_NAME}.${RouteProcessContentEntry.POSITION} = ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.POSITION} " +
                    "LEFT JOIN ${RouteEntry.TABLE_NAME} ON ${RouteEntry.TABLE_NAME}.${RouteEntry.ID} = ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} " +
                    "LEFT JOIN ${AssetEntry.TABLE_NAME} ON ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.ASSET_ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${RouteCompositionEntry.TABLE_NAME}.${RouteCompositionEntry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} "
    }
}