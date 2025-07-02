package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.user.UserPermission
import com.dacosys.assetControl.data.room.dto.user.UserPermissionEntry
import com.dacosys.assetControl.data.room.entity.user.UserPermissionEntity

@Dao
interface UserPermissionDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${UserPermissionEntry.USER_ID} = :userId AND ${UserPermissionEntry.PERMISSION_ID} = :permissionId")
    suspend fun selectByUserIdUserPermissionId(userId: Long, permissionId: Long): UserPermission?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: UserPermissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<UserPermissionEntity>)

    @Transaction
    suspend fun insert(entities: List<UserPermission>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(UserPermissionEntity(entity))
            completedTask(index + 1)
        }
    }

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${UserPermissionEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${UserPermissionEntry.TABLE_NAME}"
    }
}