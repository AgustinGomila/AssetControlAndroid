package com.dacosys.assetControl.data.room.entity.maintenance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.MAINTENANCE_TYPE_GROUP_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MAINTENANCE_TYPE_GROUP_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class MaintenanceTypeEntity(
    @PrimaryKey @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_GROUP_ID) val maintenanceTypeGroupId: Long
) {
    constructor(m: MaintenanceType) : this(
        id = m.id,
        description = m.description,
        active = m.active,
        maintenanceTypeGroupId = m.maintenanceTypeGroupId
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
            r.add("ALTER TABLE manteinance_type RENAME TO manteinance_type_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `maintenance_type`
            (
                `_id`                       INTEGER NOT NULL,
                `description`               TEXT    NOT NULL,
                `active`                    INTEGER NOT NULL,
                `maintenance_type_group_id` INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO maintenance_type (
                `_id`, `description`, `active`, `maintenance_type_group_id`
            )
            SELECT
                `_id`, `description`, `active`, `manteinance_type_group_id`
            FROM manteinance_type_temp
        """.trimIndent()
            )
            r.add("DROP TABLE manteinance_type_temp")
            r.add("DROP INDEX IF EXISTS `IDX_manteinance_type_manteinance_type_group_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_manteinance_type_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_maintenance_type_group_id` ON `maintenance_type` (`maintenance_type_group_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_description` ON `maintenance_type` (`description`);")
            return r
        }
    }
}