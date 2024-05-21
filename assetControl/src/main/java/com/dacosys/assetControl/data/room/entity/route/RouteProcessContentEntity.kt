package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ROUTE_PROCESS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_ID}"
        ),
        Index(
            value = [Entry.LEVEL],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}"
        ),
        Index(
            value = [Entry.POSITION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.POSITION}"
        ),
        Index(
            value = [Entry.ROUTE_PROCESS_STATUS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_STATUS_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        )
    ]
)
data class RouteProcessContentEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) var position: Int = 0,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_STATUS_ID) var routeProcessStatusId: Int = 0,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
) {
    constructor(r: RouteProcessContent) : this(
        id = r.id,
        routeProcessId = r.routeProcessId,
        dataCollectionRuleId = r.dataCollectionRuleId,
        level = r.level,
        position = r.position,
        routeProcessStatusId = r.routeProcessStatusId,
        dataCollectionId = r.dataCollectionId,
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
            r.add("ALTER TABLE route_process_content RENAME TO route_process_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route_process_content`
            (
                `_id`                      INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `route_process_id`         INTEGER NOT NULL,
                `data_collection_rule_id`  INTEGER NOT NULL,
                `level`                    INTEGER NOT NULL,
                `position`                 INTEGER NOT NULL,
                `route_process_status_id`  INTEGER NOT NULL,
                `data_collection_id`       INTEGER
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO route_process_content (
                `_id`, `route_process_id`,
                `data_collection_rule_id`, `level`, `position`,
                `route_process_status_id`, `data_collection_id`
            )
            SELECT
                `route_process_content_id`, `route_process_id`,
                `data_collection_rule_id`, `level`, `position`,
                `route_process_status_id`, `data_collection_id`
            FROM route_process_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_process_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_route_process_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_data_collection_rule_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_level`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_position`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_route_process_status_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_data_collection_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_content_route_process_content_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_route_process_id` ON `route_process_content` (`route_process_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_data_collection_rule_id` ON `route_process_content` (`data_collection_rule_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_level` ON `route_process_content` (`level`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_position` ON `route_process_content` (`position`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_route_process_status_id` ON `route_process_content` (`route_process_status_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_content_data_collection_id` ON `route_process_content` (`data_collection_id`);")
            return r
        }
    }
}