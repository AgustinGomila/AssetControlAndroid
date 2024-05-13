package com.dacosys.assetControl.data.room.entity.barcode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.BARCODE_LABEL_TARGET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.BARCODE_LABEL_TARGET_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelCustom(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.BARCODE_LABEL_TARGET_ID) val barcodeLabelTargetId: Long,
    @ColumnInfo(name = Entry.TEMPLATE) val template: String
) {
    object Entry {
        const val TABLE_NAME = "barcode_label_custom"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val BARCODE_LABEL_TARGET_ID = "barcode_label_target_id"
        const val TEMPLATE = "template"
    }
}