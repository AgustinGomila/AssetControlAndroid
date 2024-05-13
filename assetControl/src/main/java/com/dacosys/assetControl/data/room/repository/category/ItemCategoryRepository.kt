package com.dacosys.assetControl.data.room.repository.category

import com.dacosys.assetControl.data.room.dao.category.ItemCategoryDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemCategoryRepository {
    private val dao: ItemCategoryDao by lazy {
        database.itemCategoryDao()
    }

    fun getAllItemCategories() = dao.getAllItemCategories()

    suspend fun insertItemCategory(itemCategory: ItemCategory) {
        withContext(Dispatchers.IO) {
            dao.insertItemCategory(itemCategory)
        }
    }

    suspend fun insertAll(itemCategories: List<ItemCategory>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(itemCategories)
        }
    }

    suspend fun updateItemCategory(itemCategory: ItemCategory) {
        withContext(Dispatchers.IO) {
            dao.updateItemCategory(itemCategory)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}