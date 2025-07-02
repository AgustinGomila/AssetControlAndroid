package com.dacosys.assetControl.data.room.dto.attribute

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.attribute.AttributeCategoryObject

abstract class AttributeCategoryEntry {
    companion object {
        const val TABLE_NAME = "attribute_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
    }
}

class AttributeCategory(
    @ColumnInfo(name = AttributeCategoryEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = AttributeCategoryEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = AttributeCategoryEntry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = AttributeCategoryEntry.PARENT_ID) val parentId: Long = 0
) {
    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttributeCategory

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    constructor(acObject: AttributeCategoryObject) : this(
        id = acObject.attributeCategoryId,
        description = acObject.description,
        active = acObject.active,
        parentId = acObject.parentId
    )
}
