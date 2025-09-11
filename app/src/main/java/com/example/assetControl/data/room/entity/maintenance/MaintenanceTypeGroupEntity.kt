package com.example.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeGroup
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeGroupEntry

@Entity(
    tableName = MaintenanceTypeGroupEntry.TABLE_NAME,
    indices = [
        Index(
            value = [MaintenanceTypeGroupEntry.DESCRIPTION],
            name = "IDX_${MaintenanceTypeGroupEntry.TABLE_NAME}_${MaintenanceTypeGroupEntry.DESCRIPTION}"
        )
    ]
)
data class MaintenanceTypeGroupEntity(
    @PrimaryKey
    @ColumnInfo(name = MaintenanceTypeGroupEntry.ID) val id: Long,
    @ColumnInfo(name = MaintenanceTypeGroupEntry.DESCRIPTION) val description: String,
    @ColumnInfo(name = MaintenanceTypeGroupEntry.ACTIVE) val active: Int
) {
    constructor(m: MaintenanceTypeGroup) : this(
        id = m.id,
        description = m.description,
        active = m.active
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
            r.add("ALTER TABLE manteinance_type_group RENAME TO manteinance_type_group_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `maintenance_type_group`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO maintenance_type_group (
                `_id`, `description`, `active`
            )
            SELECT
                `_id`, `description`, `active`
            FROM manteinance_type_group_temp
        """.trimIndent()
            )
            r.add("DROP TABLE manteinance_type_group_temp")
            r.add("DROP INDEX IF EXISTS `IDX_manteinance_type_group_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_group_description` ON `maintenance_type_group` (`description`);")
            return r
        }
    }
}