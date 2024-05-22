package com.dacosys.assetControl.data.room.dto.attribute

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.attribute.AttributeObject

data class Attribute(
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.ATTRIBUTE_TYPE_ID) val attributeTypeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_CATEGORY_ID) val attributeCategoryId: Long = 0L,
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

    object Entry {
        const val TABLE_NAME = "attribute"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val ATTRIBUTE_TYPE_ID = "attribute_type_id"
        const val ATTRIBUTE_CATEGORY_ID = "attribute_category_id"
    }

    constructor(attrObj: AttributeObject) : this(
        id = attrObj.attributeId,
        description = attrObj.description,
        active = attrObj.active,
        attributeTypeId = attrObj.attributeTypeId,
        attributeCategoryId = attrObj.attributeCategoryId
    )
}
