package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategory
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategory.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeCategoryDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAttributeCategories(): Flow<List<AttributeCategory>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getById(id: Long): Flow<AttributeCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributeCategory(attributeCategory: AttributeCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attributeCategories: List<AttributeCategory>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
