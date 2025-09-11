package com.example.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.dto.route.RouteComposition
import com.example.assetControl.data.room.dto.route.RouteCompositionEntry
import com.example.assetControl.data.room.entity.route.RouteCompositionEntity

@Dao
interface RouteCompositionDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${RouteCompositionEntry.ROUTE_ID} = :routeId")
    suspend fun selectByRouteId(routeId: Long): List<RouteComposition>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<RouteCompositionEntity>)


    @Query("DELETE FROM ${RouteCompositionEntry.TABLE_NAME} WHERE ${RouteCompositionEntry.ROUTE_ID} = :id")
    suspend fun deleteByRouteId(id: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${RouteCompositionEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${RouteCompositionEntry.TABLE_NAME}"
    }
}