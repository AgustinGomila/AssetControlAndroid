package com.example.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.maintenance.MaintenanceStatus
import com.example.assetControl.data.room.dto.maintenance.MaintenanceStatusEntry
import com.example.assetControl.data.enums.maintenance.MaintenanceStatus as MaintenanceStatusEnum

@Entity(
    tableName = MaintenanceStatusEntry.TABLE_NAME,
    indices = [
        Index(
            value = [MaintenanceStatusEntry.ID],
            name = "IDX_${MaintenanceStatusEntry.TABLE_NAME}_${MaintenanceStatusEntry.ID}"
        ),
        Index(
            value = [MaintenanceStatusEntry.DESCRIPTION],
            name = "IDX_${MaintenanceStatusEntry.TABLE_NAME}_${MaintenanceStatusEntry.DESCRIPTION}"
        )
    ]
)
data class MaintenanceStatusEntity(
    @PrimaryKey @ColumnInfo(name = MaintenanceStatusEntry.ID) val id: Int = 0,
    @ColumnInfo(name = MaintenanceStatusEntry.DESCRIPTION) val description: String = ""
) {
    constructor(m: MaintenanceStatus) : this(
        id = m.id,
        description = m.description
    )

    constructor(m: MaintenanceStatusEnum) : this(
        id = m.id,
        description = m.description
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
            r.add("ALTER TABLE manteinance_status RENAME TO manteinance_status_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `maintenance_status`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO maintenance_status (
                _id, description
            )
            SELECT
                _id, description
            FROM manteinance_status_temp
        """.trimIndent()
            )
            r.add("DROP TABLE manteinance_status_temp")
            r.add("DROP INDEX IF EXISTS `IDX_manteinance_status__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_manteinance_status_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_maintenance_status__id` ON `maintenance_status` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_maintenance_status_description` ON `maintenance_status` (`description`);")
            return r
        }
    }
}