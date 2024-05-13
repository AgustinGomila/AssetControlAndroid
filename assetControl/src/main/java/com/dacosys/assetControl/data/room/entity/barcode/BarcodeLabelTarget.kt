package com.dacosys.assetControl.data.room.entity.barcode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelTarget(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Int,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String
) {
    object Entry {
        const val TABLE_NAME = "barcode_label_target"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

