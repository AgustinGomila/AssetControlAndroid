package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.attribute.Attribute.Entry
import com.dacosys.assetControl.data.webservice.attribute.AttributeObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ATTRIBUTE_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_TYPE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_CATEGORY_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class Attribute(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.ATTRIBUTE_TYPE_ID) val attributeTypeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_CATEGORY_ID) val attributeCategoryId: Long = 0L,
) {
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
