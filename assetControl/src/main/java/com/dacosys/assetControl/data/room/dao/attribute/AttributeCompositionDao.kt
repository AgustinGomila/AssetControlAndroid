package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.dto.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.dto.attribute.AttributeComposition.Entry
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCompositionEntity

@Dao
interface AttributeCompositionDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    suspend fun select(): List<AttributeComposition>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun selectById(id: Long): AttributeComposition?

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ATTRIBUTE_ID} = :attrId")
    suspend fun selectByAttributeId(attrId: Long): List<AttributeComposition>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<AttributeCompositionEntity>)


    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ATTRIBUTE_ID} IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
