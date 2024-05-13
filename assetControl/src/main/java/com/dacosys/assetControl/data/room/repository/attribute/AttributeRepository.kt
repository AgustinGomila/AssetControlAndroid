package com.dacosys.assetControl.data.room.repository.attribute

import com.dacosys.assetControl.data.room.dao.attribute.AttributeDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.attribute.Attribute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AttributeRepository {
    private val dao: AttributeDao by lazy {
        database.attributeDao()
    }

    fun getAllAttributes(): Flow<List<Attribute>> = dao.getAllAttributes()

    fun getAttributeById(id: Long): Flow<Attribute> = dao.getAttributeById(id)

    suspend fun insertAttribute(attribute: Attribute) {
        withContext(Dispatchers.IO) {
            dao.insertAttribute(attribute)
        }
    }

    suspend fun insertAll(attributes: List<Attribute>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(attributes)
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