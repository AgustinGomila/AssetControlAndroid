package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.attribute.Attribute
import com.dacosys.assetControl.data.room.entity.attribute.Attribute.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAttributes(): Flow<List<Attribute>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getAttributeById(id: Long): Flow<Attribute>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttribute(attribute: Attribute)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attributes: List<Attribute>)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}