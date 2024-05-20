package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection.Entry
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import java.util.*

@Dao
interface DataCollectionDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    suspend fun selectByCollectorId(id: Long): DataCollection?

    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $NO_TRANSFERRED_LEFT_JOIN $NO_TRANSFERRED_WHERE")
    suspend fun selectByNoTransferred(): List<DataCollection>

    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollection)


    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, ${Entry.TRANSFERRED_DATE} = :date WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(oldValue: Long, newValue: Long, date: Date = Date())


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} NOT IN ( " +
                "SELECT ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_ID} FROM ${rpEntry.TABLE_NAME}) "
    )
    suspend fun deleteOrphans()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID}"

        private val rpEntry = RouteProcess.Entry
        private val aEntry = Asset.Entry
        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry

        const val NO_TRANSFERRED_WHERE =
            "WHERE (${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NULL) AND " +
                    "(${Entry.TABLE_NAME}.${Entry.COMPLETED} = 1) AND " +
                    "(((${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NOT NULL) " +
                    "AND(${rpEntry.TABLE_NAME}.${rpEntry.COMPLETED} = 1) " +
                    "AND (${rpEntry.TABLE_NAME}.${rpEntry.TRANSFERRED_DATE} IS NULL)) " +
                    "OR (${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NULL))"

        const val NO_TRANSFERRED_LEFT_JOIN =
            " LEFT OUTER JOIN ${rpEntry.TABLE_NAME} ON ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_ID} = ${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} " +
                    "OR (${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NULL)"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${aEntry.TABLE_NAME} ON ${aEntry.TABLE_NAME}.${aEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ASSET_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} ON ${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} "

        const val BASIC_JOIN_FIELDS = "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} AS ${Entry.ASSET_STR}," +
                "${aEntry.TABLE_NAME}.${aEntry.CODE} AS ${Entry.ASSET_CODE}," +
                "${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_AREA_STR}," +
                "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}"
    }
}