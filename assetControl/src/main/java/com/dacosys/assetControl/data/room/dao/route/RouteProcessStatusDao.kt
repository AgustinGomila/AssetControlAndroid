package com.dacosys.assetControl.data.room.dao.route

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus.Entry

// DAO
@Dao
interface RouteProcessStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statuses: List<RouteProcessStatus>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}

