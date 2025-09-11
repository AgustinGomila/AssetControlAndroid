package com.example.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenance
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenanceEntry

@Entity(
    tableName = AssetMaintenanceEntry.TABLE_NAME,
    indices = [
        Index(
            value = [AssetMaintenanceEntry.ASSET_ID],
            name = "IDX_${AssetMaintenanceEntry.TABLE_NAME}_${AssetMaintenanceEntry.ASSET_ID}"
        ),
        Index(
            value = [AssetMaintenanceEntry.MAINTENANCE_STATUS_ID],
            name = "IDX_${AssetMaintenanceEntry.TABLE_NAME}_${AssetMaintenanceEntry.MAINTENANCE_STATUS_ID}"
        ),
        Index(
            value = [AssetMaintenanceEntry.ASSET_MAINTENANCE_ID],
            name = "IDX_${AssetMaintenanceEntry.TABLE_NAME}_${AssetMaintenanceEntry.ASSET_MAINTENANCE_ID}"
        ),
        Index(
            value = [AssetMaintenanceEntry.MAINTENANCE_TYPE_ID],
            name = "IDX_${AssetMaintenanceEntry.TABLE_NAME}_${AssetMaintenanceEntry.MAINTENANCE_TYPE_ID}"
        ),
        Index(
            value = [AssetMaintenanceEntry.ID],
            name = "IDX_${AssetMaintenanceEntry.TABLE_NAME}_${AssetMaintenanceEntry.ID}"
        )
    ]
)
data class AssetMaintenanceEntity(
    @PrimaryKey
    @ColumnInfo(name = AssetMaintenanceEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetMaintenanceEntry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = AssetMaintenanceEntry.OBSERVATIONS) var observations: String? = null,
    @ColumnInfo(name = AssetMaintenanceEntry.TRANSFERRED) var mTransferred: Int? = null,
    @ColumnInfo(name = AssetMaintenanceEntry.MAINTENANCE_STATUS_ID) var maintenanceStatusId: Int = 0,
    @ColumnInfo(name = AssetMaintenanceEntry.ASSET_MAINTENANCE_ID) var assetMaintenanceId: Long = 0L,
    @ColumnInfo(name = AssetMaintenanceEntry.MAINTENANCE_TYPE_ID) var maintenanceTypeId: Long = 0L,
) {
    constructor(a: AssetMaintenance) : this(
        id = a.id,
        assetId = a.assetId,
        observations = a.observations,
        mTransferred = a.mTransferred,
        maintenanceStatusId = a.maintenanceStatusId,
        assetMaintenanceId = a.assetMaintenanceId,
        maintenanceTypeId = a.maintenanceTypeId,
    )

    companion object {
        /**
         * Migration zero
         * Migración desde la base de datos SQLite (version 0) a la primera versión de Room.
         * No utilizar constantes para la definición de nombres para evitar incoherencias en el futuro.
         * @return
         */
        fun migrationZero(): List<String> {
            val r: ArrayList<String> = arrayListOf()
            r.add("ALTER TABLE asset_manteinance_collector RENAME TO asset_manteinance_collector_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset_maintenance`
            (
                `_id`                   INTEGER NOT NULL,
                `asset_id`              INTEGER NOT NULL,
                `observations`          TEXT,
                `transferred`           INTEGER,
                `maintenance_status_id` INTEGER NOT NULL,
                `asset_maintenance_id`  INTEGER NOT NULL,
                `maintenance_type_id`   INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO asset_maintenance (
                _id, asset_id, observations, transferred,
                maintenance_status_id, asset_maintenance_id,
                maintenance_type_id
            )
            SELECT
                _id, asset_id, observations, transfered,
                manteinance_status_id, asset_manteinance_id,
                manteinance_type_id
            FROM asset_manteinance_collector_temp
        """.trimIndent()
            )
            r.add("DROP TABLE asset_manteinance_collector_temp")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_manteinance_status_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_asset_manteinance_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_manteinance_type_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector__id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_id` ON `asset_maintenance` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_status_id` ON `asset_maintenance` (`maintenance_status_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_maintenance_id` ON `asset_maintenance` (`asset_maintenance_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_type_id` ON `asset_maintenance` (`maintenance_type_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance__id` ON `asset_maintenance` (`_id`);")
            return r
        }
    }
}