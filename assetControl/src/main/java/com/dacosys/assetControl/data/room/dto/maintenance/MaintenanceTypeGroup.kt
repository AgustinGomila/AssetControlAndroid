package com.dacosys.assetControl.data.room.dto.maintenance

import androidx.room.ColumnInfo

class MaintenanceTypeGroup(
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int
) {

    override fun toString(): String {
        return description
    }

    object Entry {
        const val TABLE_NAME = "maintenance_type_group"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }
}

