package com.dacosys.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroup.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}")
    ]
)
data class MaintenanceTypeGroup(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int
) {
    object Entry {
        const val TABLE_NAME = "manteinance_type_group"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }
}

