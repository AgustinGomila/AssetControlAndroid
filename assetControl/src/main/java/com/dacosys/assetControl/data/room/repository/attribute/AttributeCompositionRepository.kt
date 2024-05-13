package com.dacosys.assetControl.data.room.repository.attribute

import com.dacosys.assetControl.data.room.dao.attribute.AttributeCompositionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AttributeCompositionRepository {
    private val dao: AttributeCompositionDao by lazy {
        database.attributeCompositionDao()
    }

    fun getAllAttributeCompositions(): Flow<List<AttributeComposition>> = dao.getAllAttributeCompositions()

    fun getById(id: Long): Flow<AttributeComposition> = dao.getById(id)

    suspend fun insertAttributeComposition(attributeComposition: AttributeComposition) {
        withContext(Dispatchers.IO) {
            dao.insertAttributeComposition(attributeComposition)
        }
    }

    suspend fun insertAll(attributeCompositions: List<AttributeComposition>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(attributeCompositions)
        }
    }

    suspend fun updateAttributeComposition(attributeComposition: AttributeComposition) {
        withContext(Dispatchers.IO) {
            dao.updateAttributeComposition(attributeComposition)
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
