package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.dto.user.UserEntry
import com.dacosys.assetControl.data.room.dto.user.UserPermissionEntry
import com.dacosys.assetControl.data.room.entity.user.UserEntity

@Dao
interface UserDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN")
    suspend fun select(): List<User>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${UserEntry.TABLE_NAME}.${UserEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): User?

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${UserEntry.TABLE_NAME}.${UserEntry.EMAIL} = :mailOrName " +
                "OR ${UserEntry.TABLE_NAME}.${UserEntry.NAME} = :mailOrName"
    )
    suspend fun selectByNameOrEmail(mailOrName: String): User?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<UserEntity>)

    @Transaction
    suspend fun insert(entities: List<User>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(UserEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(user: UserEntity)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${UserEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${UserEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${UserEntry.TABLE_NAME}.${UserEntry.NAME}"
        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${UserPermissionEntry.TABLE_NAME} ON ${UserEntry.TABLE_NAME}.${UserEntry.ID} = ${UserPermissionEntry.TABLE_NAME}.${UserPermissionEntry.USER_ID}"
    }
}
