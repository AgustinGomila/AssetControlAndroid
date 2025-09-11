package com.example.assetControl.data.room.entity.movement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContentEntry

@Entity(
    tableName = WarehouseMovementContentEntry.TABLE_NAME,
    indices = [
        Index(
            value = [WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID],
            name = "IDX_${WarehouseMovementContentEntry.TABLE_NAME}_${WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID}"
        ),
        Index(
            value = [WarehouseMovementContentEntry.ASSET_ID],
            name = "IDX_${WarehouseMovementContentEntry.TABLE_NAME}_${WarehouseMovementContentEntry.ASSET_ID}"
        ),
        Index(
            value = [WarehouseMovementContentEntry.CODE],
            name = "IDX_${WarehouseMovementContentEntry.TABLE_NAME}_${WarehouseMovementContentEntry.CODE}"
        )
    ]
)
data class WarehouseMovementContentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = WarehouseMovementContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = WarehouseMovementContentEntry.CODE) var code: String = "",
    @ColumnInfo(name = WarehouseMovementContentEntry.QTY) var qty: Double? = null,
) {
    constructor(w: WarehouseMovementContent) : this(
        id = w.id,
        warehouseMovementId = w.warehouseMovementId,
        assetId = w.assetId,
        code = w.code,
        qty = w.qty,
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
            r.add("ALTER TABLE warehouse_movement_content RENAME TO warehouse_movement_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_movement_content`
            (
                `_id`                   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `warehouse_movement_id` INTEGER NOT NULL,
                `asset_id`              INTEGER NOT NULL,
                `code`                  TEXT    NOT NULL,
                `qty`                   REAL
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_movement_content (
                `_id`, `warehouse_movement_id`,
                `asset_id`, `code`, `qty`
            )
            SELECT
                `_id`, `warehouse_movement_id`,
                `asset_id`, `code`, `qty`
            FROM warehouse_movement_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_movement_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_warehouse_movement_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_code`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_warehouse_movement_id` ON `warehouse_movement_content` (`warehouse_movement_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_asset_id` ON `warehouse_movement_content` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_code` ON `warehouse_movement_content` (`code`);")
            return r
        }
    }
}