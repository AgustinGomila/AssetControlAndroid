package com.dacosys.assetControl.data.room.dto.attribute

import androidx.room.ColumnInfo

class AttributeComposition(
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) val attributeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String? = null,
    @ColumnInfo(name = Entry.COMPOSITION) val composition: String? = null,
    @ColumnInfo(name = Entry.USED) val used: Int = 0,
    @ColumnInfo(name = Entry.NAME) val name: String = "",
    @ColumnInfo(name = Entry.READ_ONLY) val readOnly: Int = 0,
    @ColumnInfo(name = Entry.DEFAULT_VALUE) val defaultValue: String = "",
) {
    override fun toString(): String {
        return description.orEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttributeComposition

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    object Entry {
        const val TABLE_NAME = "attribute_composition"
        const val ID = "_id"
        const val ATTRIBUTE_ID = "attribute_id"
        const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
        const val DESCRIPTION = "description"
        const val COMPOSITION = "composition"
        const val USED = "used"
        const val NAME = "name"
        const val READ_ONLY = "read_only"
        const val DEFAULT_VALUE = "default_value"
    }
}
