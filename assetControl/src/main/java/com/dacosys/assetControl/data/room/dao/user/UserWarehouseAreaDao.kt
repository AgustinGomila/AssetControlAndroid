package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea.Entry

@Dao
interface UserWarehouseAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userWarehouseArea: UserWarehouseArea)

    @Update
    suspend fun update(userWarehouseArea: UserWarehouseArea)

    @Delete
    suspend fun delete(userWarehouseArea: UserWarehouseArea)

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.USER_ID} = :userId")
    fun getUserWarehouseAreasByUserId(userId: Long): List<UserWarehouseArea>
}

