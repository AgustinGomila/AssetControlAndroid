package com.dacosys.assetControl.data.room.dao.category

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import com.dacosys.assetControl.data.room.entity.category.ItemCategory.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCategoryDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllItemCategories(): Flow<List<ItemCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemCategory(itemCategory: ItemCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemCategories: List<ItemCategory>)

    @Update
    suspend fun updateItemCategory(itemCategory: ItemCategory)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}