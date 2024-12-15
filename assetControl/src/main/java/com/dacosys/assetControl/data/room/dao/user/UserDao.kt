package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.dto.user.User.Entry
import com.dacosys.assetControl.data.room.entity.user.UserEntity
import com.dacosys.assetControl.data.room.dto.user.UserPermission.Entry as upEntry

@Dao
interface UserDao {
    @Query("$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN")
    suspend fun select(): List<User>

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): User?

    @Query(
        "$BASIC_SELECT $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.EMAIL} = :mailOrName " +
                "OR ${Entry.TABLE_NAME}.${Entry.NAME} = :mailOrName"
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
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.NAME}"
        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${upEntry.TABLE_NAME} ON ${Entry.TABLE_NAME}.${Entry.ID} = ${upEntry.TABLE_NAME}.${upEntry.USER_ID}"
    }
}
