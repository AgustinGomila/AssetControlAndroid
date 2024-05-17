package com.dacosys.assetControl.data.room.dao.attribute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dacosys.assetControl.data.room.entity.attribute.Attribute

@Dao
interface AttributeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attribute: Attribute)

    @Transaction
    suspend fun insert(entities: List<Attribute>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }
}