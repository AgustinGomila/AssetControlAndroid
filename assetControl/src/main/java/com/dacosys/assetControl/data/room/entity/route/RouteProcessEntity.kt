package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.dto.route.RouteProcess.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.ROUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_ID}"
        ),
        Index(
            value = [Entry.ROUTE_PROCESS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}"
        )
    ]
)
data class RouteProcessEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_ID) var routeId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_DATE) var routeProcessDate: Date = Date(),
    @ColumnInfo(name = Entry.COMPLETED) var mCompleted: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
) {
    constructor(r: RouteProcess) : this(
        id = r.id,
        userId = r.userId,
        routeId = r.routeId,
        routeProcessId = r.routeProcessId,
        routeProcessDate = r.routeProcessDate,
        mCompleted = r.mCompleted,
        transferred = r.transferred,
        transferredDate = r.transferredDate,
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
            r.add("ALTER TABLE route_process RENAME TO route_process_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route_process`
            (
                `_id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `user_id`            INTEGER NOT NULL,
                `route_id`           INTEGER NOT NULL,
                `route_process_id`   INTEGER NOT NULL,
                `route_process_date` INTEGER NOT NULL,
                `completed`          INTEGER NOT NULL,
                `transferred`        INTEGER,
                `transferred_date`   INTEGER
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO route_process (
                `user_id`, `route_id`, `route_process_id`,
                `route_process_date`, `completed`,
                `transferred`, `transferred_date`, `_id`
            )
            SELECT
                `user_id`, `route_id`, `route_process_id`,
                `route_process_date`, `completed`,
                `transfered`, `transfered_date`, `_id`
            FROM route_process_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_process_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_route_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_route_process_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_user_id` ON `route_process` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_route_id` ON `route_process` (`route_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_route_process_id` ON `route_process` (`route_process_id`);")
            return r
        }
    }
}