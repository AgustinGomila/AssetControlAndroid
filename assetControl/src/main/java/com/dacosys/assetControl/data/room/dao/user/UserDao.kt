package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.entity.user.User
import com.dacosys.assetControl.data.room.entity.user.User.Entry
import com.dacosys.assetControl.data.room.entity.user.UserPermission.Entry as upEntry

@Dao
interface UserDao {
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

    @Query(
        "$BASIC_SELECT $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 " +
                "AND ${upEntry.TABLE_NAME}.${upEntry.PERMISSION_ID} = :permission"
    )
    suspend fun selectByActiveAndPermission(permission: Long = PermissionEntry.UseCollectorProgram.id): List<User>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<User>)

    @Transaction
    suspend fun insert(entities: List<User>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(user: User)


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
