package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.entity.route.RouteProcessStatus.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class RouteProcessStatus(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {
    object Entry {
        const val TABLE_NAME = "route_process_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }

    constructor(status: RouteProcessStatus) : this(
        id = status.id,
        description = status.description
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
            r.add("ALTER TABLE route_process_status RENAME TO route_process_status_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route_process_status`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO route_process_status (
                _id, description
            )
            SELECT
                _id, description
            FROM route_process_status_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_process_status_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_status__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_status_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_status__id` ON `route_process_status` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_status_description` ON `route_process_status` (`description`);")
            return r
        }
    }
}

