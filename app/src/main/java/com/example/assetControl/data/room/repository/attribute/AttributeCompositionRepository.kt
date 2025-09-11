package com.example.assetControl.data.room.repository.attribute

import com.example.assetControl.data.room.dao.attribute.AttributeCompositionDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.attribute.AttributeComposition
import com.example.assetControl.data.room.entity.attribute.AttributeCompositionEntity
import kotlinx.coroutines.runBlocking

class AttributeCompositionRepository {
    private val dao: AttributeCompositionDao
        get() = database.attributeCompositionDao()

    fun select() = runBlocking { dao.select() }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByAttributeId(id: Long) = runBlocking { dao.selectByAttributeId(id) }


    fun insert(compositions: List<AttributeComposition>) = runBlocking {
        dao.insert(compositions.map { AttributeCompositionEntity(it) })
    }


    fun deleteByIds(ids: List<Long>) {
        runBlocking {
            dao.deleteByIds(ids)
        }
    }
}
