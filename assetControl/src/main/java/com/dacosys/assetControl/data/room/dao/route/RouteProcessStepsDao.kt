package com.dacosys.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.dto.route.RouteProcessSteps
import com.dacosys.assetControl.data.room.dto.route.RouteProcessSteps.Entry
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStepsEntity

@Dao
interface RouteProcessStepsDao {

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ROUTE_PROCESS_ID} = :routeProcessId $BASIC_ORDER")
    suspend fun selectByRouteProcessId(routeProcessId: Long): List<RouteProcessSteps>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: RouteProcessStepsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<RouteProcessStepsEntity>)


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ROUTE_PROCESS_ID} = :id")
    suspend fun deleteByRouteProcessId(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.DATA_COLLECTION_ID} = :id")
    suspend fun deleteByCollectorDataCollectionId(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_ID} FROM ${rpEntry.TABLE_NAME} " +
                "WHERE ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_DATE} < :routeProcessDate " +
                "AND ${rpEntry.TABLE_NAME}.${rpEntry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_ID} = :routeId )"
    )
    suspend fun deleteByRouteIdRouteProcessDate(routeProcessDate: String, routeId: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ROUTE_PROCESS_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.LEVEL}, " +
                "${Entry.TABLE_NAME}.${Entry.POSITION}, " +
                "${Entry.TABLE_NAME}.${Entry.STEP}"

        private val rpEntry = RouteProcess.Entry
    }
}