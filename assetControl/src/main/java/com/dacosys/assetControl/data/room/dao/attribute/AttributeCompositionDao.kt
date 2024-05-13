package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeCompositionDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAttributeCompositions(): Flow<List<AttributeComposition>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getById(id: Long): Flow<AttributeComposition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributeComposition(attributeComposition: AttributeComposition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attributeCompositions: List<AttributeComposition>)

    @Update
    suspend fun updateAttributeComposition(attributeComposition: AttributeComposition)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
