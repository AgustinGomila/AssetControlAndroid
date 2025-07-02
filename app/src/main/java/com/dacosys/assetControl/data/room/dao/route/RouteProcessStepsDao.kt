package com.dacosys.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.route.RouteProcessEntry
import com.dacosys.assetControl.data.room.dto.route.RouteProcessSteps
import com.dacosys.assetControl.data.room.dto.route.RouteProcessStepsEntry
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStepsEntity

@Dao
interface RouteProcessStepsDao {

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${RouteProcessStepsEntry.TABLE_NAME}.${RouteProcessStepsEntry.ROUTE_PROCESS_ID} = :routeProcessId $BASIC_ORDER")
    suspend fun selectByRouteProcessId(routeProcessId: Long): List<RouteProcessSteps>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: RouteProcessStepsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<RouteProcessStepsEntity>)


    @Query("DELETE $BASIC_FROM WHERE ${RouteProcessStepsEntry.ROUTE_PROCESS_ID} = :id")
    suspend fun deleteByRouteProcessId(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${RouteProcessStepsEntry.DATA_COLLECTION_ID} = :id")
    suspend fun deleteByCollectorDataCollectionId(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${RouteProcessStepsEntry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_PROCESS_ID} FROM ${RouteProcessEntry.TABLE_NAME} " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_PROCESS_DATE} < :routeProcessDate " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} = :routeId )"
    )
    suspend fun deleteByRouteIdRouteProcessDate(routeProcessDate: String, routeId: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${RouteProcessStepsEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${RouteProcessStepsEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${RouteProcessStepsEntry.TABLE_NAME}.${RouteProcessStepsEntry.ROUTE_PROCESS_ID}, " +
                    "${RouteProcessStepsEntry.TABLE_NAME}.${RouteProcessStepsEntry.LEVEL}, " +
                    "${RouteProcessStepsEntry.TABLE_NAME}.${RouteProcessStepsEntry.POSITION}, " +
                    "${RouteProcessStepsEntry.TABLE_NAME}.${RouteProcessStepsEntry.STEP}"
    }
}