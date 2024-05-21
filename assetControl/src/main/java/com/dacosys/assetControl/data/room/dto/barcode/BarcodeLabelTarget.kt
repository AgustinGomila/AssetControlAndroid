package com.dacosys.assetControl.data.room.dto.barcode

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.enums.barcode.BarcodeLabelTarget

data class BarcodeLabelTarget(
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {
    override fun toString(): String {
        return description
    }

    constructor(target: BarcodeLabelTarget) : this(
        id = target.id,
        description = target.description
    )

    object Entry {
        const val TABLE_NAME = "barcode_label_target"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

