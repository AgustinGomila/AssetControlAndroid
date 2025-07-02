package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.asset.AssetEntry
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseEntry
import com.dacosys.assetControl.data.room.dto.route.RouteProcessEntry
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionEntity
import java.util.*

@Dao
interface DataCollectionDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): DataCollection?

    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $NO_TRANSFERRED_LEFT_JOIN $NO_TRANSFERRED_WHERE")
    suspend fun selectByNoTransferred(): List<DataCollection>

    @Query("SELECT MIN(${DataCollectionEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollectionEntity)


    @Query(
        "UPDATE ${DataCollectionEntry.TABLE_NAME} SET ${DataCollectionEntry.DATA_COLLECTION_ID} = :dataCollectionId, " +
                "${DataCollectionEntry.TRANSFERRED_DATE} = :date " +
                "WHERE ${DataCollectionEntry.ID} = :oldValue"
    )
    suspend fun updateDataCollectionId(dataCollectionId: Long, oldValue: Long, date: Date)


    @Query("DELETE $BASIC_FROM WHERE ${DataCollectionEntry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} NOT IN ( " +
                "SELECT ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ID} FROM ${RouteProcessEntry.TABLE_NAME}) "
    )
    suspend fun deleteOrphans()

    companion object {
        const val BASIC_SELECT = "SELECT ${DataCollectionEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${DataCollectionEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.DATA_COLLECTION_ID}"

        const val NO_TRANSFERRED_WHERE =
            " WHERE (${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.TRANSFERRED_DATE} IS NULL) " +
                    "AND (${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.COMPLETED} = 1) " +
                    "AND (((${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} IS NOT NULL) " +
                    "AND (${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.COMPLETED} = 1) " +
                    "AND (${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NULL)) " +
                    "OR (${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} IS NULL))"

        const val NO_TRANSFERRED_LEFT_JOIN =
            " LEFT OUTER JOIN ${RouteProcessEntry.TABLE_NAME} ON ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ID} = ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} " +
                    "OR (${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ROUTE_PROCESS_ID} IS NULL)"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${AssetEntry.TABLE_NAME} ON ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.ASSET_ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${DataCollectionEntry.TABLE_NAME}.${DataCollectionEntry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} "

        const val BASIC_JOIN_FIELDS =
            "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} AS ${DataCollectionEntry.ASSET_STR}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} AS ${DataCollectionEntry.ASSET_CODE}," +
                    "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${DataCollectionEntry.WAREHOUSE_AREA_STR}," +
                    "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${DataCollectionEntry.WAREHOUSE_STR}"
    }
}