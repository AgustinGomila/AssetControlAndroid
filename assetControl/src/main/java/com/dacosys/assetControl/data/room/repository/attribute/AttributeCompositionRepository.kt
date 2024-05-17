package com.dacosys.assetControl.data.room.repository.attribute

import com.dacosys.assetControl.data.room.dao.attribute.AttributeCompositionDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import kotlinx.coroutines.runBlocking

class AttributeCompositionRepository {
    private val dao: AttributeCompositionDao
        get() = database.attributeCompositionDao()

    fun select() = dao.select()

    fun selectById(id: Long) = dao.selectById(id)

    fun selectByAttributeId(id: Long) = dao.selectByAttributeId(id)


    fun insert(compositions: List<AttributeComposition>) = runBlocking {
        dao.insert(compositions)
    }


    fun deleteByIds(ids: List<Long>) {
        runBlocking {
            dao.deleteByIds(ids)
        }
    }
}
