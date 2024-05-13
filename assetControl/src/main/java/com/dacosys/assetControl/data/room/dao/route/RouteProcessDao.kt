package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import com.dacosys.assetControl.data.room.entity.route.RouteProcess.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteProcessDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllRouteProcesses(): Flow<List<RouteProcess>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteProcess(routeProcess: RouteProcess)

    @Delete
    suspend fun deleteRouteProcess(routeProcess: RouteProcess)
}