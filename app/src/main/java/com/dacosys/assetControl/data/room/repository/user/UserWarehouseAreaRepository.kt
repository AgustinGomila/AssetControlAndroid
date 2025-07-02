package com.dacosys.assetControl.data.room.repository.user

import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.user.UserWarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseArea
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class UserWarehouseAreaRepository {
    private val dao: UserWarehouseAreaDao
        get() = database.userWarehouseAreaDao()


    fun insert(contents: List<UserWarehouseAreaObject>, progress: (SyncProgress) -> Unit) {
        runBlocking {
            val total = contents.size

            val upList: ArrayList<UserWarehouseArea> = arrayListOf()
            contents.mapTo(upList) { UserWarehouseArea(it) }

            dao.insert(upList) {
                progress.invoke(
                    SyncProgress(
                        totalTask = total,
                        completedTask = it,
                        msg = context.getString(R.string.synchronizing_user_areas),
                        registryType = SyncRegistryType.UserWarehouseArea,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }


    fun updateWarehouseAreaId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateWarehouseAreaId(newValue, oldValue)
        }
    }


    fun deleteAll() = runBlocking(Dispatchers.IO) {
        dao.deleteAll()
    }
}