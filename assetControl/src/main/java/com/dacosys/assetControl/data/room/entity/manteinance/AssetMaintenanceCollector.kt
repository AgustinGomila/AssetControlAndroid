package com.dacosys.assetControl.data.room.entity.manteinance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.manteinance.AssetMaintenanceCollector.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.MAINTENANCE_STATUS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MAINTENANCE_STATUS_ID}"
        ),
        Index(
            value = [Entry.ASSET_MAINTENANCE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_MAINTENANCE_ID}"
        ),
        Index(
            value = [Entry.MAINTENANCE_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MAINTENANCE_TYPE_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        )
    ]
)
data class AssetMaintenanceCollector(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long,
    @ColumnInfo(name = Entry.OBSERVATIONS) val observations: String?,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int?,
    @ColumnInfo(name = Entry.MAINTENANCE_STATUS_ID) val maintenanceStatusId: Long,
    @ColumnInfo(name = Entry.ASSET_MAINTENANCE_ID) val assetMaintenanceId: Long,
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_ID) val maintenanceTypeId: Long
) {
    object Entry {
        const val TABLE_NAME = "asset_maintenance_collector"
        const val ID = "_id"
        const val ASSET_ID = "asset_id"
        const val OBSERVATIONS = "observations"
        const val TRANSFERRED = "transfered"
        const val MAINTENANCE_STATUS_ID = "maintenance_status_id"
        const val ASSET_MAINTENANCE_ID = "asset_maintenance_id"
        const val MAINTENANCE_TYPE_ID = "maintenance_type_id"
    }
}
