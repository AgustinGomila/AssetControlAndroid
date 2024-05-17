package com.dacosys.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.enums.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatus.Entry

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
data class MaintenanceStatus(
    @PrimaryKey @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {
    object Entry {
        const val TABLE_NAME = "manteinance_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }

    constructor(maintenanceStatus: MaintenanceStatus) : this(
        id = maintenanceStatus.id,
        description = maintenanceStatus.description
    )
}