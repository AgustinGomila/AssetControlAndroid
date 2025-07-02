package com.dacosys.assetControl.data.room.dto.barcode

import androidx.room.ColumnInfo

abstract class BarcodeLabelTargetEntry {
    companion object {
        const val TABLE_NAME = "barcode_label_target"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

data class BarcodeLabelTarget(
    @ColumnInfo(name = BarcodeLabelTargetEntry.ID) val id: Int = 0,
    @ColumnInfo(name = BarcodeLabelTargetEntry.DESCRIPTION) val description: String = ""
) {
    override fun toString(): String {
        return description
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarcodeLabelTarget

        return id == other.id
    }

    constructor(target: BarcodeLabelTarget) : this(
        id = target.id,
        description = target.description
    )
}

