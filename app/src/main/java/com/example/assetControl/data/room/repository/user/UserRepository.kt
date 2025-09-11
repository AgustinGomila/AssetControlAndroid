package com.example.assetControl.data.room.repository.user

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.user.UserDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.user.User
import com.example.assetControl.data.webservice.user.UserObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UserRepository {
    private val dao: UserDao
        get() = database.userDao()

    fun select() = runBlocking { dao.select() }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByNameOrEmail(text: String) = runBlocking { dao.selectByNameOrEmail(text) }


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
                        msg = context.getString(R.string.synchronizing_users),
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
