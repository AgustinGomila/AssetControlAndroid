package com.example.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.assetControl.data.room.dto.attribute.AttributeCategory
import com.example.assetControl.data.room.entity.attribute.AttributeCategoryEntity

@Dao
interface AttributeCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: AttributeCategoryEntity)

    @Transaction
    suspend fun insert(entities: List<AttributeCategory>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(AttributeCategoryEntity(entity))
            completedTask(index + 1)
        }
    }
}
