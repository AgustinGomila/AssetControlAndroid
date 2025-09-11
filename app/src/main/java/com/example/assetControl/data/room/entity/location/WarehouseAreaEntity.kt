package com.example.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.location.WarehouseAreaEntry

@Entity(
    tableName = WarehouseAreaEntry.TABLE_NAME,
    indices = [
        Index(
            value = [WarehouseAreaEntry.DESCRIPTION],
            name = "IDX_${WarehouseAreaEntry.TABLE_NAME}_${WarehouseAreaEntry.DESCRIPTION}"
        ),
        Index(
            value = [WarehouseAreaEntry.WAREHOUSE_ID],
            name = "IDX_${WarehouseAreaEntry.TABLE_NAME}_${WarehouseAreaEntry.WAREHOUSE_ID}"
        )
    ]
)
data class WarehouseAreaEntity(
    @PrimaryKey
    @ColumnInfo(name = WarehouseAreaEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = WarehouseAreaEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = WarehouseAreaEntry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = WarehouseAreaEntry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = WarehouseAreaEntry.TRANSFERRED) var transferred: Int? = null,
) {
    constructor(d: WarehouseArea) : this(
        id = d.id,
        description = d.description,
        mActive = d.mActive,
        warehouseId = d.warehouseId,
        transferred = d.transferred
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
            r.add("ALTER TABLE warehouse_area RENAME TO warehouse_area_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_area`
            (
                `_id`          INTEGER NOT NULL,
                `description`  TEXT    NOT NULL,
                `active`       INTEGER NOT NULL,
                `warehouse_id` INTEGER NOT NULL,
                `transferred`  INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_area (
                `_id`, `description`, `active`,
                `warehouse_id`, `transferred` 
            )
            SELECT
                `_id`, `description`, `active`,
                `warehouse_id`, `transferred`
            FROM warehouse_area_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_area_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_area_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_area_warehouse_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_description` ON `warehouse_area` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_warehouse_id` ON `warehouse_area` (`warehouse_id`)")
            return r
        }
    }
}