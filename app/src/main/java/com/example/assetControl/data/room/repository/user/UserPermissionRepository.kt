package com.example.assetControl.data.room.repository.user

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.user.UserPermissionDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.user.UserPermission
import com.example.assetControl.data.webservice.user.UserPermissionObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class UserPermissionRepository {
    private val dao: UserPermissionDao
        get() = database.userPermissionDao()

    fun selectByUserIdUserPermissionId(userId: Long, permissionId: Long): UserPermission? = runBlocking {
        dao.selectByUserIdUserPermissionId(userId, permissionId)
    }


    fun insert(contents: List<UserPermissionObject>, progress: (SyncProgress) -> Unit) {
        runBlocking {
            val total = contents.size

            val upList: ArrayList<UserPermission> = arrayListOf()
            contents.mapTo(upList) { UserPermission(it) }

            dao.insert(upList) {
                progress.invoke(
                    SyncProgress(
                        totalTask = total,
                        completedTask = it,
                        msg = context.getString(R.string.synchronizing_user_permissions),
                        registryType = SyncRegistryType.UserPermission,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }


    fun deleteAll() = runBlocking(Dispatchers.IO) {
        dao.deleteAll()
    }
}