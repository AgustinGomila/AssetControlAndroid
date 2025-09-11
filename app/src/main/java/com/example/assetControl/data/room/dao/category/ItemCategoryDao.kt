package com.example.assetControl.data.room.dao.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.dto.category.ItemCategoryEntry
import com.example.assetControl.data.room.entity.category.ItemCategoryEntity

@Dao
interface ItemCategoryDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<ItemCategory>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${ItemCategoryEntry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<ItemCategory>

    @Query("SELECT MIN(${ItemCategoryEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${ItemCategoryEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): ItemCategory?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${ItemCategoryEntry.TRANSFERRED} = 0 $BASIC_ORDER"
    )
    suspend fun selectNoTransferred(): List<ItemCategory>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(itemCategory: ItemCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categories: List<ItemCategoryEntity>)

    @Transaction
    suspend fun insert(entities: List<ItemCategory>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(ItemCategoryEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(category: ItemCategoryEntity)

    @Query("UPDATE ${ItemCategoryEntry.TABLE_NAME} SET ${ItemCategoryEntry.ID} = :newValue WHERE ${ItemCategoryEntry.ID} = :oldValue")
    suspend fun updateId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${ItemCategoryEntry.TABLE_NAME} " +
                "SET ${ItemCategoryEntry.TRANSFERRED} = 1 " +
                "WHERE ${ItemCategoryEntry.ID} IN (:ids)"
    )
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Delete
    suspend fun delete(itemCategory: ItemCategoryEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        private const val PARENT_ALIAS = "parent_cat"
        private const val ALIAS = "cat"

        const val BASIC_SELECT = "SELECT $ALIAS.*"
        const val BASIC_FROM = "FROM ${ItemCategoryEntry.TABLE_NAME}"
        const val BASIC_FROM_ALIAS = "FROM ${ItemCategoryEntry.TABLE_NAME} $ALIAS"
        const val BASIC_ORDER = "ORDER BY $ALIAS.${ItemCategoryEntry.DESCRIPTION}, " +
                "$PARENT_ALIAS.${ItemCategoryEntry.DESCRIPTION}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${ItemCategoryEntry.TABLE_NAME} $PARENT_ALIAS ON $ALIAS.${ItemCategoryEntry.PARENT_ID} = $PARENT_ALIAS.${ItemCategoryEntry.ID}"
        const val BASIC_JOIN_FIELDS =
            "$PARENT_ALIAS.${ItemCategoryEntry.DESCRIPTION} AS ${ItemCategoryEntry.PARENT_STR}"
    }
}