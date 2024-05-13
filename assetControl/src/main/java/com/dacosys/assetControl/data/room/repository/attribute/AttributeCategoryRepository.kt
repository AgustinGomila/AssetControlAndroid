package com.dacosys.assetControl.data.room.repository.attribute

import com.dacosys.assetControl.data.room.dao.attribute.AttributeCategoryDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttributeCategoryRepository {
    private val dao: AttributeCategoryDao by lazy {
        database.attributeCategoryDao()
    }

    fun getAllAttributeCategories() = dao.getAllAttributeCategories()

    fun getById(id: Long) = dao.getById(id)

    suspend fun insertAttributeCategory(attributeCategory: AttributeCategory) {
        withContext(Dispatchers.IO) {
            dao.insertAttributeCategory(attributeCategory)
        }
    }

    suspend fun insertAll(attributeCategories: List<AttributeCategory>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(attributeCategories)
        }
    }

    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
