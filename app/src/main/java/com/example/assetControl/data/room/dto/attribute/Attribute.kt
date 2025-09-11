package com.example.assetControl.data.room.dto.attribute

import androidx.room.ColumnInfo
import com.example.assetControl.data.webservice.attribute.AttributeObject

abstract class AttributeEntry {
    companion object {
        const val TABLE_NAME = "attribute"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val ATTRIBUTE_TYPE_ID = "attribute_type_id"
        const val ATTRIBUTE_CATEGORY_ID = "attribute_category_id"
    }
}

data class Attribute(
    @ColumnInfo(name = AttributeEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = AttributeEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = AttributeEntry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = AttributeEntry.ATTRIBUTE_TYPE_ID) val attributeTypeId: Long = 0L,
    @ColumnInfo(name = AttributeEntry.ATTRIBUTE_CATEGORY_ID) val attributeCategoryId: Long = 0L,
) {
    override fun toString(): String {
        return description
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attribute

        return id == other.id
    }

    constructor(attrObj: AttributeObject) : this(
        id = attrObj.attributeId,
        description = attrObj.description,
        active = attrObj.active,
        attributeTypeId = attrObj.attributeTypeId,
        attributeCategoryId = attrObj.attributeCategoryId
    )
}
