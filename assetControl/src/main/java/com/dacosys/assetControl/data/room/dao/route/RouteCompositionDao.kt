package com.dacosys.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.route.RouteComposition
import com.dacosys.assetControl.data.room.entity.route.RouteComposition.Entry

@Dao
interface RouteCompositionDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.ROUTE_ID} = :routeId")
    fun selectByRouteId(routeId: Long): List<RouteComposition>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: RouteComposition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<RouteComposition>)


    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ROUTE_ID} = :id")
    suspend fun deleteByRouteId(id: Long)


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}

