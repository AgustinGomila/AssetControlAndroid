package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategory.Entry
import com.dacosys.assetControl.data.webservice.attribute.AttributeCategoryObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.PARENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.PARENT_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class AttributeCategory(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.PARENT_ID) val parentId: Long = 0
) {
    object Entry {
        const val TABLE_NAME = "attribute_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
    }

    constructor(acObject: AttributeCategoryObject) : this(
        id = acObject.attributeCategoryId,
        description = acObject.description,
        active = acObject.active,
        parentId = acObject.parentId
    )
}
