package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.Route.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}")
    ]
)
data class RouteEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0
) {
    constructor(r: Route) : this(
        id = r.id,
        description = r.description,
        active = r.active
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
            r.add("ALTER TABLE route RENAME TO route_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route`
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
            INSERT INTO route (
                `_id`, `description`, `active`
            )
            SELECT
                `_id`, `description`, `active`
            FROM route_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_description` ON `route` (`description`);")
            return r
        }
    }
}