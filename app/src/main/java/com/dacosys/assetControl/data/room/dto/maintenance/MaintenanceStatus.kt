package com.dacosys.assetControl.data.room.dto.maintenance

import androidx.room.ColumnInfo

abstract class MaintenanceStatusEntry {
    companion object {
        const val TABLE_NAME = "maintenance_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

class MaintenanceStatus(
    @ColumnInfo(name = MaintenanceStatusEntry.ID) val id: Int = 0,
    @ColumnInfo(name = MaintenanceStatusEntry.DESCRIPTION) val description: String = ""
) {

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MaintenanceStatus

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    constructor(maintenanceStatus: MaintenanceStatus) : this(
        id = maintenanceStatus.id,
        description = maintenanceStatus.description
    )
}