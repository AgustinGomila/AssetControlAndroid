package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dacosys.assetControl.data.room.dto.attribute.Attribute
import com.dacosys.assetControl.data.room.entity.attribute.AttributeEntity

@Dao
interface AttributeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attribute: AttributeEntity)

    @Transaction
    suspend fun insert(entities: List<Attribute>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(AttributeEntity(entity))
            completedTask(index + 1)
        }
    }
}