package com.dacosys.assetControl.data.room.repository.user

import com.dacosys.assetControl.data.room.dao.user.UserWarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea

class UserWarehouseAreaRepository {
    private val dao: UserWarehouseAreaDao by lazy {
        database.userWarehouseAreaDao()
    }

    suspend fun insert(userWarehouseArea: UserWarehouseArea) {
        dao.insert(userWarehouseArea)
    }

    suspend fun update(userWarehouseArea: UserWarehouseArea) {
        dao.update(userWarehouseArea)
    }

    suspend fun delete(userWarehouseArea: UserWarehouseArea) {
        dao.delete(userWarehouseArea)
    }

    fun getUserWarehouseAreasByUserId(userId: Long): List<UserWarehouseArea> {
        return dao.getUserWarehouseAreasByUserId(userId)
    }
}