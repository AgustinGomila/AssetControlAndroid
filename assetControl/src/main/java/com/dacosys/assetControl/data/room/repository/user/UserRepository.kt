package com.dacosys.assetControl.data.room.repository.user

import com.dacosys.assetControl.data.room.dao.user.UserDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val dao: UserDao by lazy {
        database.userDao()
    }

    fun getAllUsers() = dao.getAllUsers()

    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            dao.insertUser(user)
        }
    }

    suspend fun insertAll(users: List<User>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(users)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            dao.updateUser(user)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
