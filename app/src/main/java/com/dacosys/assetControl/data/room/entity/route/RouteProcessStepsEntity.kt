package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.dto.route.RouteProcessSteps
import com.dacosys.assetControl.data.room.dto.route.RouteProcessStepsEntry

@Entity(
    tableName = RouteProcessStepsEntry.TABLE_NAME,
    primaryKeys = [RouteProcessStepsEntry.ROUTE_PROCESS_ID, RouteProcessStepsEntry.LEVEL, RouteProcessStepsEntry.POSITION],
    indices = [
        Index(
            value = [RouteProcessStepsEntry.ROUTE_PROCESS_ID],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.ROUTE_PROCESS_ID}"
        ),
        Index(
            value = [RouteProcessStepsEntry.ROUTE_PROCESS_CONTENT_ID],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.ROUTE_PROCESS_CONTENT_ID}"
        ),
        Index(
            value = [RouteProcessStepsEntry.LEVEL],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.LEVEL}"
        ),
        Index(
            value = [RouteProcessStepsEntry.POSITION],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.POSITION}"
        ),
        Index(
            value = [RouteProcessStepsEntry.DATA_COLLECTION_ID],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [RouteProcessStepsEntry.STEP],
            name = "IDX_${RouteProcessStepsEntry.TABLE_NAME}_${RouteProcessStepsEntry.STEP}"
        )
    ]
)
data class RouteProcessStepsEntity(
    @ColumnInfo(name = RouteProcessStepsEntry.ROUTE_PROCESS_ID) val routeProcessId: Long = 0L,
    @ColumnInfo(name = RouteProcessStepsEntry.ROUTE_PROCESS_CONTENT_ID) val routeProcessContentId: Long = 0L,
    @ColumnInfo(name = RouteProcessStepsEntry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = RouteProcessStepsEntry.POSITION) val position: Int = 0,
    @ColumnInfo(name = RouteProcessStepsEntry.DATA_COLLECTION_ID) val dataCollectionId: Long? = null,
    @ColumnInfo(name = RouteProcessStepsEntry.STEP) val step: Int = 0,
) {
    constructor(r: RouteProcessSteps) : this(
        routeProcessId = r.routeProcessId,
        routeProcessContentId = r.routeProcessContentId,
        level = r.level,
        position = r.position,
        dataCollectionId = r.dataCollectionId,
        step = r.step
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
            r.add("ALTER TABLE route_process_steps RENAME TO route_process_steps_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route_process_steps`
            (
                `route_process_id`         INTEGER NOT NULL,
                `route_process_content_id` INTEGER NOT NULL,
                `level`                    INTEGER NOT NULL,
                `position`                 INTEGER NOT NULL,
                `data_collection_id`       INTEGER,
                `step`                     INTEGER NOT NULL,
                PRIMARY KEY (`route_process_id`,`level`,`position`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO route_process_steps (
                `route_process_id`, `route_process_content_id`,
                `level`, `position`, `data_collection_id`, `step`
            )
            SELECT
                `route_process_id`, `route_process_content_id`,
                `level`, `position`, `data_collection_id`, `step`
            FROM route_process_steps_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_process_steps_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_route_process_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_route_process_content_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_level`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_position`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_data_collection_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_process_steps_step`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_route_process_id` ON `route_process_steps` (`route_process_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_route_process_content_id` ON `route_process_steps` (`route_process_content_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_level` ON `route_process_steps` (`level`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_position` ON `route_process_steps` (`position`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_data_collection_id` ON `route_process_steps` (`data_collection_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_step` ON `route_process_steps` (`step`);")

            return r
        }
    }
}