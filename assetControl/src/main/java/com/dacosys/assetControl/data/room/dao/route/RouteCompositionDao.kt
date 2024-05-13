package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteComposition
import com.dacosys.assetControl.data.room.entity.route.RouteComposition.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteCompositionDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllRouteCompositions(): Flow<List<RouteComposition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteComposition(routeComposition: RouteComposition)

    @Update
    suspend fun updateRouteComposition(routeComposition: RouteComposition)

    @Delete
    suspend fun deleteRouteComposition(routeComposition: RouteComposition)
}

