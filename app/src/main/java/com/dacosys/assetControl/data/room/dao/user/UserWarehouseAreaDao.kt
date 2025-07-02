package com.dacosys.assetControl.data.room.dao.user

import androidx.room.*
import com.dacosys.assetControl.data.room.dao.user.UserPermissionDao.Companion.BASIC_FROM
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseArea
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseAreaEntry
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


    @Query("UPDATE ${UserWarehouseAreaEntry.TABLE_NAME} SET ${UserWarehouseAreaEntry.WAREHOUSE_AREA_ID} = :newValue WHERE ${UserWarehouseAreaEntry.WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateWarehouseAreaId(newValue: Long, oldValue: Long)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()
}

