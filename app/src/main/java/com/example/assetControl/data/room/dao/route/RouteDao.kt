package com.example.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.assetControl.data.room.dto.route.Route
import com.example.assetControl.data.room.dto.route.RouteEntry
import com.example.assetControl.data.room.entity.route.RouteEntity

@Dao
interface RouteDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_ORDER")
    suspend fun select(): List<Route>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${RouteEntry.TABLE_NAME}.${RouteEntry.ACTIVE} = 1 $BASIC_ORDER")
    suspend fun selectActive(): List<Route>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${RouteEntry.TABLE_NAME}.${RouteEntry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                BASIC_ORDER
    )
    suspend fun selectByDescription(description: String): List<Route>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${RouteEntry.TABLE_NAME}.${RouteEntry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${RouteEntry.TABLE_NAME}.${RouteEntry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectByDescriptionOnlyActive(description: String): List<Route>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Transaction
    suspend fun insert(entities: List<Route>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(RouteEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(route: RouteEntity)

    @Delete
    suspend fun delete(route: RouteEntity)

    companion object {
        const val BASIC_SELECT = "SELECT *"
        const val BASIC_FROM = "FROM ${RouteEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${RouteEntry.TABLE_NAME}.${RouteEntry.DESCRIPTION}"
    }
}
