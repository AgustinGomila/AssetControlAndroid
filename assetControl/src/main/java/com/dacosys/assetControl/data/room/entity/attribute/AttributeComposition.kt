package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ATTRIBUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_COMPOSITION_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_TYPE_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class AttributeComposition(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) val attributeId: Long,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String?,
    @ColumnInfo(name = Entry.COMPOSITION) val composition: String?,
    @ColumnInfo(name = Entry.USED) val used: Int,
    @ColumnInfo(name = Entry.NAME) val name: String,
    @ColumnInfo(name = Entry.READ_ONLY) val readOnly: Int,
    @ColumnInfo(name = Entry.DEFAULT_VALUE) val defaultValue: String
) {
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
