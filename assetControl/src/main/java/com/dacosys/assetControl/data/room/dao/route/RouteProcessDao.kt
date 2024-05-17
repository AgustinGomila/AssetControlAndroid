package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import com.dacosys.assetControl.data.room.entity.route.RouteProcess.Entry
import java.util.*

@Dao
interface RouteProcessDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    fun select(): List<RouteProcess>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ROUTE_ID} = :routeId")
    fun selectByRouteIdNoCompleted(routeId: Long): List<RouteProcess>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED} = 0")
    fun selectByNoTransferred(): List<RouteProcess>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NOT NULL")
    fun selectTransferred(): List<RouteProcess>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.COMPLETED} = 0")
    fun selectByNoCompleted(): List<RouteProcess>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :rpId")
    fun selectById(rpId: Long): RouteProcess?

    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeProcess: RouteProcess)


    @Update
    suspend fun update(content: RouteProcess)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, ${Entry.TRANSFERRED_DATE} = :date WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(oldValue: Long, newValue: Long, date: Date = Date())


    @Delete
    suspend fun delete(routeProcess: RouteProcess)

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
        const val BASIC_SELECT = "SELECT *"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}