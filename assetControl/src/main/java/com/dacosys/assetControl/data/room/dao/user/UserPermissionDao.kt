package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.user.UserPermission
import com.dacosys.assetControl.data.room.entity.user.UserPermission.Entry

@Dao
interface UserPermissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userPermission: UserPermission)

    @Delete
    suspend fun delete(userPermission: UserPermission)

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.USER_ID} = :userId")
    fun getUserPermissionsByUserId(userId: Long): List<UserPermission>
}