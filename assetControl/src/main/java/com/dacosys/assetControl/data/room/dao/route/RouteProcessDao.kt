package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.dto.route.RouteProcess.Entry
import com.dacosys.assetControl.data.room.entity.route.RouteProcessEntity
import java.util.*

@Dao
interface RouteProcessDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN")
    suspend fun select(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ROUTE_ID} = :routeId " +
                "AND ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 0"
    )
    suspend fun selectByRouteIdNoCompleted(routeId: Long): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NULL " +
                "AND ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 1"
    )
    suspend fun selectByNoTransferred(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NOT NULL"
    )
    suspend fun selectTransferred(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 0"
    )
    suspend fun selectByNoCompleted(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :rpId"
    )
    suspend fun selectById(rpId: Long): RouteProcess?

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeProcess: RouteProcessEntity)


    @Update
    suspend fun update(content: RouteProcessEntity)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ROUTE_PROCESS_ID} = :routeProcessId, " +
                "${Entry.TRANSFERRED_DATE} = :date " +
                "WHERE ${Entry.ID} = :oldValue"
    )
    suspend fun updateRouteProcessId(routeProcessId: Long, oldValue: Long, date: Date)


    @Delete
    suspend fun delete(routeProcess: RouteProcessEntity)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE $BASIC_FROM WHERE ${Entry.TRANSFERRED} = 1")
    suspend fun deleteTransferred()

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${Entry.ROUTE_PROCESS_ID} FROM ${Entry.TABLE_NAME} temp_${Entry.TABLE_NAME} " +
                "WHERE ${Entry.ROUTE_PROCESS_DATE} < :minDate " +
                "AND ${Entry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${Entry.ROUTE_ID} = :routeId ) "
    )
    suspend fun deleteByRouteIdRouteProcessDate(minDate: String, routeId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"

        private val rEntry = Route.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${rEntry.TABLE_NAME} ON ${rEntry.TABLE_NAME}.${rEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ROUTE_ID} "

        const val BASIC_JOIN_FIELDS =
            "${rEntry.TABLE_NAME}.${rEntry.DESCRIPTION} AS ${Entry.ROUTE_STR}"
    }
}