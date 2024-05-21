package com.dacosys.assetControl.data.room.dao.category

import androidx.room.*
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.data.room.dto.category.ItemCategory.Entry
import com.dacosys.assetControl.data.room.entity.category.ItemCategoryEntity

@Dao
interface ItemCategoryDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<ItemCategory>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${Entry.ACTIVE} = 1 $BASIC_ORDER"
    )
    suspend fun selectActive(): List<ItemCategory>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): ItemCategory?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM_ALIAS $BASIC_LEFT_JOIN " +
                "WHERE $ALIAS.${Entry.TRANSFERRED} = 0 $BASIC_ORDER"
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

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} " +
                "SET ${Entry.TRANSFERRED} = 1 " +
                "WHERE ${Entry.ID} IN (:ids)"
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
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_FROM_ALIAS = "FROM ${Entry.TABLE_NAME} $ALIAS"
        const val BASIC_ORDER = "ORDER BY $ALIAS.${Entry.DESCRIPTION}, " +
                "$PARENT_ALIAS.${Entry.DESCRIPTION}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${Entry.TABLE_NAME} $PARENT_ALIAS ON $ALIAS.${Entry.PARENT_ID} = $PARENT_ALIAS.${Entry.ID}"
        const val BASIC_JOIN_FIELDS = "$PARENT_ALIAS.${Entry.DESCRIPTION} AS ${Entry.PARENT_STR}"
    }
}