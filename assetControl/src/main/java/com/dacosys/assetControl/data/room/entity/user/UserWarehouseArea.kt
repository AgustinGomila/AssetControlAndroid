package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea.Entry
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaObject

@Entity(
    tableName = Entry.TABLE_NAME,
    primaryKeys = [Entry.USER_ID, Entry.WAREHOUSE_AREA_ID],
    indices = [
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(value = [Entry.WAREHOUSE_AREA_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}")
    ]
)
data class UserWarehouseArea(
    @ColumnInfo(name = Entry.USER_ID) val userId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.SEE) val see: Int = 0,
    @ColumnInfo(name = Entry.MOVE) val move: Int = 0,
    @ColumnInfo(name = Entry.COUNT) val count: Int = 0,
    @ColumnInfo(name = Entry.CHECK) val check: Int = 0
) {
    object Entry {
        const val TABLE_NAME = "user_warehouse_area"
        const val USER_ID = "user_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val SEE = "see"
        const val MOVE = "move"
        const val COUNT = "count"
        const val CHECK = "check"
    }

    constructor(uwaObj: UserWarehouseAreaObject) : this(
        userId = uwaObj.user_id,
        warehouseAreaId = uwaObj.warehouse_area_id,
        see = uwaObj.see,
        move = uwaObj.move,
        count = uwaObj.count,
        check = uwaObj.check
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
