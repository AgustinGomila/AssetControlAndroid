package com.dacosys.assetControl.data.room.entity.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.category.ItemCategory.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        ),
        Index(
            value = [Entry.PARENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.PARENT_ID}"
        )
    ]
)
data class ItemCategory(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.PARENT_ID) val parentId: Long
) {
    object Entry {
        const val TABLE_NAME = "item_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
    }
}
