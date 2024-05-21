package com.dacosys.assetControl.data.room.dto.attribute

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.attribute.AttributeCategoryObject

class AttributeCategory(
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.PARENT_ID) val parentId: Long = 0
) {
    override fun toString(): String {
        return description
    }

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
