package com.dacosys.assetControl.data.room.repository.user

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.user.UserDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.user.User
import com.dacosys.assetControl.data.webservice.user.UserObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UserRepository {
    private val dao: UserDao
        get() = database.userDao()

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByNameOrEmail(text: String) = runBlocking { dao.selectByNameOrEmail(text) }

    fun selectByActiveAndPermission() = runBlocking { dao.selectByActiveAndPermission() }


    suspend fun sync(
        assetsObj: Array<UserObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.User

        val users: ArrayList<User> = arrayListOf()
        assetsObj.mapTo(users) { User(it) }
        val partial = users.count()

        withContext(Dispatchers.IO) {
            dao.insert(users) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = getContext().getString(R.string.synchronizing_users),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }


    fun deleteAll() {
        runBlocking(Dispatchers.IO) {
            deleteAllSuspend()
        }
    }

    private suspend fun deleteAllSuspend() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
