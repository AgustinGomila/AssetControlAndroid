package com.dacosys.assetControl.data.room.repository.user

import com.dacosys.assetControl.data.room.dao.user.UserPermissionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.user.UserPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPermissionRepository {
    private val dao: UserPermissionDao by lazy {
        database.userPermissionDao()
    }

    suspend fun insert(userPermission: UserPermission) {
        withContext(Dispatchers.IO) {
            dao.insert(userPermission)
        }
    }

    suspend fun delete(userPermission: UserPermission) {
        withContext(Dispatchers.IO) {
            dao.delete(userPermission)
        }
    }

    fun getUserPermissionsByUserId(userId: Long): List<UserPermission> {
        return dao.getUserPermissionsByUserId(userId)
    }
}