package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus.Entry
import kotlinx.coroutines.flow.Flow

// DAO
@Dao
interface RouteProcessStatusDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllRouteProcessStatus(): Flow<List<RouteProcessStatus>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRouteProcessStatus(routeProcessStatus: RouteProcessStatus)

    @Delete
    suspend fun deleteRouteProcessStatus(routeProcessStatus: RouteProcessStatus)
}

