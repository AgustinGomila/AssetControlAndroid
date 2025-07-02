package com.dacosys.assetControl.data.room.entity.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseEntry

@Entity(
    tableName = WarehouseEntry.TABLE_NAME,
    indices = [
        Index(
            value = [WarehouseEntry.DESCRIPTION],
            name = "IDX_${WarehouseEntry.TABLE_NAME}_${WarehouseEntry.DESCRIPTION}"
        ),
    ]
)
data class WarehouseEntity(
    @PrimaryKey
    @ColumnInfo(name = WarehouseEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = WarehouseEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = WarehouseEntry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = WarehouseEntry.TRANSFERRED) val transferred: Int? = null
) {
    constructor(w: Warehouse) : this(
        id = w.id,
        description = w.description,
        mActive = w.mActive,
        transferred = w.transferred
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
            r.add("ALTER TABLE warehouse RENAME TO warehouse_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                `transferred` INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse (
                `_id`, `description`, `active`, `transferred`
            )
            SELECT
                `_id`, `description`, `active`, `transferred`
            FROM warehouse_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_description` ON `warehouse` (`description`);")
            return r
        }
    }
}