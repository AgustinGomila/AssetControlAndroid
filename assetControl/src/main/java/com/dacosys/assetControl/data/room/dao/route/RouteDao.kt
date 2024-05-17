package com.dacosys.assetControl.data.room.dao.route

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.Route
import com.dacosys.assetControl.data.room.entity.route.Route.Entry

@Dao
interface RouteDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_ORDER")
    fun select(): List<Route>

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 $BASIC_ORDER")
    fun selectActive(): List<Route>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                BASIC_ORDER
    )
    fun selectByDescription(description: String): List<Route>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%'  " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    fun selectByDescriptionOnlyActive(description: String): List<Route>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: Route)

    @Transaction
    suspend fun insert(entities: List<Route>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(route: Route)

    @Delete
    suspend fun delete(route: Route)

    companion object {
        const val BASIC_SELECT = "SELECT *"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}"
    }
}
