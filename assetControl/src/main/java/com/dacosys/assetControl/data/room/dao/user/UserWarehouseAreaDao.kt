package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.dao.user.UserPermissionDao.Companion.BASIC_FROM
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseArea
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseArea.Entry
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseAreaEntity

@Dao
interface UserWarehouseAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: UserWarehouseAreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<UserWarehouseAreaEntity>)

    @Transaction
    suspend fun insert(entities: List<UserWarehouseArea>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(UserWarehouseAreaEntity(entity))
            completedTask(index + 1)
        }
    }


    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_AREA_ID} = :newValue WHERE ${Entry.WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateWarehouseAreaId(oldValue: Long, newValue: Long)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()
}

