package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.RouteComposition
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent.Entry
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContentEntity

@Dao
interface RouteProcessContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<RouteProcessContent>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): RouteProcessContent?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ROUTE_PROCESS_ID} = :id"
    )
    suspend fun selectByRouteProcessId(id: Long): List<RouteProcessContent>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: RouteProcessContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: List<RouteProcessContentEntity>): List<Long>


    @Update
    suspend fun update(content: RouteProcessContentEntity)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} " +
                "SET ${Entry.ROUTE_PROCESS_STATUS_ID} = :processStatusId, ${Entry.DATA_COLLECTION_ID} = :dataCollectionId " +
                "WHERE ${Entry.ROUTE_PROCESS_ID} = :routeProcessId " +
                "AND ${Entry.ID} = :id " +
                "AND ${Entry.DATA_COLLECTION_RULE_ID} = :dataCollectionRuleId " +
                "AND ${Entry.LEVEL} = :level " +
                "AND ${Entry.POSITION} = :position "
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
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ROUTE_PROCESS_ID} = :newValue " +
                "WHERE ${Entry.ROUTE_PROCESS_ID} = :oldValue"
    )
    suspend fun updateRouteProcessId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.DATA_COLLECTION_ID} = :newValue " +
                "WHERE ${Entry.DATA_COLLECTION_ID} = :oldValue"
    )
    suspend fun updateDataCollectionId(newValue: Long, oldValue: Long)


    @Delete
    suspend fun delete(content: RouteProcessContentEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${Entry.ROUTE_PROCESS_ID} = :id")
    suspend fun deleteByRouteProcessId(id: Long)


    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${rpEntry.ROUTE_PROCESS_ID} FROM ${rpEntry.TABLE_NAME} " +
                "WHERE ${rpEntry.ROUTE_PROCESS_DATE} < :routeProcessDate " +
                "AND ${rpEntry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${rpEntry.ROUTE_ID} = :routeId )"
    )
    suspend fun deleteByRouteIdRouteProcessDate(routeProcessDate: String, routeId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ROUTE_PROCESS_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.LEVEL}, " +
                "${Entry.TABLE_NAME}.${Entry.POSITION}"

        private val aEntry = Asset.Entry
        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry
        private val rEntry = Route.Entry
        private val rpEntry = RouteProcess.Entry
        private val rcEntry = RouteComposition.Entry

        const val BASIC_JOIN_FIELDS =
            "${rcEntry.TABLE_NAME}.${rcEntry.ASSET_ID} AS ${Entry.ASSET_ID}, " +
                    "${aEntry.TABLE_NAME}.${aEntry.CODE} AS ${Entry.ASSET_CODE}, " +
                    "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} AS ${Entry.ASSET_STR}, " +
                    "${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_ID} AS ${Entry.ROUTE_ID}, " +
                    "${rEntry.TABLE_NAME}.${rEntry.DESCRIPTION} AS ${Entry.ROUTE_STR}, " +
                    "${rcEntry.TABLE_NAME}.${rcEntry.WAREHOUSE_ID} AS ${Entry.WAREHOUSE_ID}, " +
                    "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}, " +
                    "${rcEntry.TABLE_NAME}.${rcEntry.WAREHOUSE_AREA_ID} AS ${Entry.WAREHOUSE_AREA_ID}, " +
                    "${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_AREA_STR} "


        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${rpEntry.TABLE_NAME} ON ${rpEntry.TABLE_NAME}.${rpEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ROUTE_PROCESS_ID} " +
                    "LEFT JOIN ${rcEntry.TABLE_NAME} ON ${rcEntry.TABLE_NAME}.${rcEntry.ROUTE_ID} = ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_ID} AND " +
                    "${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_ID} = ${rcEntry.TABLE_NAME}.${rcEntry.DATA_COLLECTION_RULE_ID} AND " +
                    "${Entry.TABLE_NAME}.${Entry.LEVEL} = ${rcEntry.TABLE_NAME}.${rcEntry.LEVEL} AND " +
                    "${Entry.TABLE_NAME}.${Entry.POSITION} = ${rcEntry.TABLE_NAME}.${rcEntry.POSITION} " +
                    "LEFT JOIN ${rEntry.TABLE_NAME} ON ${rEntry.TABLE_NAME}.${rEntry.ID} = ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_ID} " +
                    "LEFT JOIN ${aEntry.TABLE_NAME} ON ${aEntry.TABLE_NAME}.${aEntry.ID} = ${rcEntry.TABLE_NAME}.${rcEntry.ASSET_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} ON ${waEntry.TABLE_NAME}.${waEntry.ID} = ${rcEntry.TABLE_NAME}.${rcEntry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} "
    }
}