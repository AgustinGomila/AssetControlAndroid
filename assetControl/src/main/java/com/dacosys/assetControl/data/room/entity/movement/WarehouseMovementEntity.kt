package com.dacosys.assetControl.data.room.entity.movement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.WAREHOUSE_MOVEMENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_MOVEMENT_ID}"
        ),
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.ORIGIN_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.ORIGIN_WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_ID}"
        ),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_ID}"
        )
    ]
)
data class WarehouseMovementEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_DATE) var warehouseMovementDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_ID) var originWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_ID) var destinationWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_ID) var destinationWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int? = null,
) {
    constructor(w: WarehouseMovement) : this(
        id = w.id,
        warehouseMovementId = w.warehouseMovementId,
        warehouseMovementDate = w.warehouseMovementDate,
        obs = w.obs,
        userId = w.userId,
        originWarehouseAreaId = w.originWarehouseAreaId,
        originWarehouseId = w.originWarehouseId,
        transferredDate = w.transferredDate,
        destinationWarehouseAreaId = w.destinationWarehouseAreaId,
        destinationWarehouseId = w.destinationWarehouseId,
        completed = w.completed,
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
            r.add("ALTER TABLE warehouse_movement RENAME TO warehouse_movement_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_movement`
            (
                `_id`                           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `warehouse_movement_id`         INTEGER NOT NULL,
                `warehouse_movement_date`       INTEGER NOT NULL,
                `obs`                           TEXT,
                `user_id`                       INTEGER NOT NULL,
                `origin_warehouse_area_id`      INTEGER NOT NULL,
                `origin_warehouse_id`           INTEGER NOT NULL,
                `transferred_date`              INTEGER,
                `destination_warehouse_area_id` INTEGER NOT NULL,
                `destination_warehouse_id`      INTEGER NOT NULL,
                `completed`                     INTEGER
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_movement (
                `_id`, `warehouse_movement_id`, `warehouse_movement_date`, `obs`, `user_id`,
                `origin_warehouse_area_id`, `origin_warehouse_id`, `transferred_date`,
                `destination_warehouse_area_id`, `destination_warehouse_id`, `completed`
            )
            SELECT
                `_id`, `warehouse_movement_id`, `warehouse_movement_date`, `obs`, `user_id`,
                `origin_warehouse_area_id`, `origin_warehouse_id`, `transfered_date`,
                `destination_warehouse_area_id`, `destination_warehouse_id`, `completed`
            FROM warehouse_movement_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_movement_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_warehouse_movement_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_origin_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_origin_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_destination_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_destination_warehouse_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_warehouse_movement_id` ON `warehouse_movement` (`warehouse_movement_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_user_id` ON `warehouse_movement` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_area_id` ON `warehouse_movement` (`origin_warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_id` ON `warehouse_movement` (`origin_warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_area_id` ON `warehouse_movement` (`destination_warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_id` ON `warehouse_movement` (`destination_warehouse_id`);")
            return r
        }
    }
}