package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.user.UserPermission
import com.dacosys.assetControl.data.room.entity.user.UserPermission.Entry

@Dao
interface UserPermissionDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.USER_ID} = :userId AND ${Entry.PERMISSION_ID} = :permissionId")
    suspend fun selectByUserIdUserPermissionId(userId: Long, permissionId: Long): UserPermission?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: UserPermission)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<UserPermission>)

    @Transaction
    suspend fun insert(entities: List<UserPermission>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}