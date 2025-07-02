package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseArea
import com.dacosys.assetControl.data.room.dto.user.UserWarehouseAreaEntry

@Entity(
    tableName = UserWarehouseAreaEntry.TABLE_NAME,
    primaryKeys = [UserWarehouseAreaEntry.USER_ID, UserWarehouseAreaEntry.WAREHOUSE_AREA_ID],
    indices = [
        Index(
            value = [UserWarehouseAreaEntry.USER_ID],
            name = "IDX_${UserWarehouseAreaEntry.TABLE_NAME}_${UserWarehouseAreaEntry.USER_ID}"
        ),
        Index(
            value = [UserWarehouseAreaEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${UserWarehouseAreaEntry.TABLE_NAME}_${UserWarehouseAreaEntry.WAREHOUSE_AREA_ID}"
        )
    ]
)
data class UserWarehouseAreaEntity(
    @ColumnInfo(name = UserWarehouseAreaEntry.USER_ID) val userId: Long = 0L,
    @ColumnInfo(name = UserWarehouseAreaEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long = 0L,
    @ColumnInfo(name = UserWarehouseAreaEntry.SEE) val see: Int = 0,
    @ColumnInfo(name = UserWarehouseAreaEntry.MOVE) val move: Int = 0,
    @ColumnInfo(name = UserWarehouseAreaEntry.COUNT) val count: Int = 0,
    @ColumnInfo(name = UserWarehouseAreaEntry.CHECK) val check: Int = 0
) {
    constructor(u: UserWarehouseArea) : this(
        userId = u.userId,
        warehouseAreaId = u.warehouseAreaId,
        see = u.see,
        move = u.move,
        count = u.count,
        check = u.check
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
            r.add("ALTER TABLE user_warehouse_area RENAME TO user_warehouse_area_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `user_warehouse_area`
            (
                `user_id`           INTEGER NOT NULL,
                `warehouse_area_id` INTEGER NOT NULL,
                `see`               INTEGER NOT NULL,
                `move`              INTEGER NOT NULL,
                `count`             INTEGER NOT NULL,
                `check`             INTEGER NOT NULL,
                PRIMARY KEY (`user_id`, `warehouse_area_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO user_warehouse_area (
                `user_id`, `warehouse_area_id`,
                `see`, `move`, `count`, `check`
            )
            SELECT
                `user_id`, `warehouse_area_id`,
                `see`, `move`, `count`, `check`
            FROM user_warehouse_area_temp
        """.trimIndent()
            )
            r.add("DROP TABLE user_warehouse_area_temp")
            r.add("DROP INDEX IF EXISTS `IDX_user_warehouse_area_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_user_warehouse_area_warehouse_area_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_warehouse_area_user_id` ON `user_warehouse_area` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_warehouse_area_warehouse_area_id` ON `user_warehouse_area` (`warehouse_area_id`);")
            return r
        }
    }
}