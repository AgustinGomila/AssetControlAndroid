package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.route.RouteEntry
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.dto.route.RouteProcessEntry
import com.dacosys.assetControl.data.room.entity.route.RouteProcessEntity
import java.util.*

@Dao
interface RouteProcessDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN")
    suspend fun select(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} = :routeId " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.COMPLETED} = 0 " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NULL"
    )
    suspend fun selectByRouteIdNoCompleted(routeId: Long): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NULL " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.COMPLETED} = 1"
    )
    suspend fun selectByNoTransferred(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NOT NULL"
    )
    suspend fun selectTransferred(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.TRANSFERRED_DATE} IS NULL " +
                "AND ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.COMPLETED} = 0"
    )
    suspend fun selectByNoCompleted(): List<RouteProcess>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): RouteProcess?

    @Query("SELECT MIN(${RouteProcessEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeProcess: RouteProcessEntity)


    @Update
    suspend fun update(content: RouteProcessEntity)

    @Query(
        "UPDATE ${RouteProcessEntry.TABLE_NAME} SET ${RouteProcessEntry.ROUTE_PROCESS_ID} = :routeProcessId, " +
                "${RouteProcessEntry.TRANSFERRED_DATE} = :date " +
                "WHERE ${RouteProcessEntry.ID} = :oldValue"
    )
    suspend fun updateRouteProcessId(routeProcessId: Long, oldValue: Long, date: Date)


    @Delete
    suspend fun delete(routeProcess: RouteProcessEntity)

    @Query("DELETE $BASIC_FROM WHERE ${RouteProcessEntry.ID} = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE $BASIC_FROM WHERE ${RouteProcessEntry.TRANSFERRED} = 1")
    suspend fun deleteTransferred()

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${RouteProcessEntry.ROUTE_PROCESS_ID} IN ( " +
                "SELECT ${RouteProcessEntry.ROUTE_PROCESS_ID} FROM ${RouteProcessEntry.TABLE_NAME} temp_${RouteProcessEntry.TABLE_NAME} " +
                "WHERE ${RouteProcessEntry.ROUTE_PROCESS_DATE} < :minDate " +
                "AND ${RouteProcessEntry.TRANSFERRED_DATE} IS NOT NULL " +
                "AND ${RouteProcessEntry.ROUTE_ID} = :routeId ) "
    )
    suspend fun deleteByRouteIdRouteProcessDate(minDate: String, routeId: Long)

    companion object {
        const val BASIC_SELECT = "SELECT ${RouteProcessEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${RouteProcessEntry.TABLE_NAME}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${RouteEntry.TABLE_NAME} ON ${RouteEntry.TABLE_NAME}.${RouteEntry.ID} = ${RouteProcessEntry.TABLE_NAME}.${RouteProcessEntry.ROUTE_ID} "

        const val BASIC_JOIN_FIELDS =
            "${RouteEntry.TABLE_NAME}.${RouteEntry.DESCRIPTION} AS ${RouteProcessEntry.ROUTE_STR}"
    }
}