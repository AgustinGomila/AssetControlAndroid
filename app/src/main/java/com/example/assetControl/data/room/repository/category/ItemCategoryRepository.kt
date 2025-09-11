package com.example.assetControl.data.room.repository.category

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.category.ItemCategoryDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.entity.category.ItemCategoryEntity
import com.example.assetControl.data.webservice.category.ItemCategoryObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
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

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(category: ItemCategory) = runBlocking {
        category.id = nextLastId
        category.transferred = 0
        dao.insert(ItemCategoryEntity(category))
    }


    fun update(category: ItemCategory) = runBlocking {
        category.transferred = 0
        dao.update(ItemCategoryEntity(category))
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
                        msg = context.getString(R.string.synchronizing_categories),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}