package com.dacosys.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.route.RouteProcessSteps
import com.dacosys.assetControl.data.room.entity.route.RouteProcessSteps.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteProcessStepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteProcessSteps(routeProcessSteps: RouteProcessSteps)

    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllRouteProcessSteps(): Flow<List<RouteProcessSteps>>
}