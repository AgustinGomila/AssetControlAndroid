package com.dacosys.assetControl.data.room.repository.category

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.category.ItemCategoryDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ItemCategoryRepository {
    private val dao: ItemCategoryDao
        get() = database.itemCategoryDao()

    fun select(onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectActive()
        else dao.select()
    }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectNoTransferred() = runBlocking { dao.selectNoTransferred() }

    val minId get() = runBlocking { dao.selectMinId() ?: -1 }


    fun insert(category: ItemCategory) {
        runBlocking {
            dao.insert(category)
        }
    }


    fun update(category: ItemCategory): Boolean {
        val r = runBlocking {
            dao.update(category)
            true
        }
        return r
    }

    fun updateTransferred(ids: List<Long>): Boolean {
        val r = runBlocking {
            dao.updateTransferred(ids.toTypedArray())
            true
        }
        return r
    }

    fun updateId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateId(newValue, oldValue)
    }

    suspend fun sync(
        icObj: Array<ItemCategoryObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.ItemCategory

        val categories: ArrayList<ItemCategory> = arrayListOf()
        icObj.mapTo(categories) { ItemCategory(it) }
        val partial = categories.count()

        withContext(Dispatchers.IO) {
            dao.insert(categories) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = getContext().getString(R.string.synchronizing_categories),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}