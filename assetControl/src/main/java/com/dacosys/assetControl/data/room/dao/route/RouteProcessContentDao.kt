package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteProcessContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteProcessContent(routeProcessContent: RouteProcessContent)

    @Update
    suspend fun updateRouteProcessContent(routeProcessContent: RouteProcessContent)

    @Delete
    suspend fun deleteRouteProcessContent(routeProcessContent: RouteProcessContent)

    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllRouteProcessContent(): Flow<List<RouteProcessContent>>
}
