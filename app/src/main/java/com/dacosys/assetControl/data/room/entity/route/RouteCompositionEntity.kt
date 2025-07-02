package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.dto.route.RouteComposition
import com.dacosys.assetControl.data.room.dto.route.RouteCompositionEntry

@Entity(
    tableName = RouteCompositionEntry.TABLE_NAME,
    primaryKeys = [RouteCompositionEntry.ROUTE_ID, RouteCompositionEntry.LEVEL, RouteCompositionEntry.POSITION],
    indices = [
        Index(
            value = [RouteCompositionEntry.ROUTE_ID],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.ROUTE_ID}"
        ),
        Index(
            value = [RouteCompositionEntry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.DATA_COLLECTION_RULE_ID}"
        ),
        Index(
            value = [RouteCompositionEntry.LEVEL],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.LEVEL}"
        ),
        Index(
            value = [RouteCompositionEntry.POSITION],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.POSITION}"
        ),
        Index(
            value = [RouteCompositionEntry.ASSET_ID],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.ASSET_ID}"
        ),
        Index(
            value = [RouteCompositionEntry.WAREHOUSE_ID],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.WAREHOUSE_ID}"
        ),
        Index(
            value = [RouteCompositionEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${RouteCompositionEntry.TABLE_NAME}_${RouteCompositionEntry.WAREHOUSE_AREA_ID}"
        )
    ]
)
data class RouteCompositionEntity(
    @ColumnInfo(name = RouteCompositionEntry.ROUTE_ID) val routeId: Long = 0L,
    @ColumnInfo(name = RouteCompositionEntry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = RouteCompositionEntry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.POSITION) val position: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.ASSET_ID) val assetId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.WAREHOUSE_ID) val warehouseId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long? = null,
    @ColumnInfo(name = RouteCompositionEntry.EXPRESSION) val expression: String? = null,
    @ColumnInfo(name = RouteCompositionEntry.TRUE_RESULT) val trueResult: Int = 0,
    @ColumnInfo(name = RouteCompositionEntry.FALSE_RESULT) val falseResult: Int = 0
) {
    constructor(r: RouteComposition) : this(
        routeId = r.routeId,
        dataCollectionRuleId = r.dataCollectionRuleId,
        level = r.level,
        position = r.position,
        assetId = r.assetId,
        warehouseId = r.warehouseId,
        warehouseAreaId = r.warehouseAreaId,
        expression = r.expression,
        trueResult = r.trueResult,
        falseResult = r.falseResult
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
            r.add("ALTER TABLE route_composition RENAME TO route_composition_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `route_composition`
            (
                `route_id`                INTEGER NOT NULL,
                `data_collection_rule_id` INTEGER NOT NULL,
                `level`                   INTEGER NOT NULL,
                `position`                INTEGER NOT NULL,
                `asset_id`                INTEGER,
                `warehouse_id`            INTEGER,
                `warehouse_area_id`       INTEGER,
                `expression`              TEXT,
                `true_result`             INTEGER NOT NULL,
                `false_result`            INTEGER NOT NULL,
                PRIMARY KEY (`route_id`, `level`, `position`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO route_composition (
                `route_id`, `data_collection_rule_id`,
                `level`, `position`,
                `asset_id`, `warehouse_id`, `warehouse_area_id`,
                `expression`, `true_result`, `false_result`
            )
            SELECT
                `route_id`, `data_collection_rule_id`,
                `level`, `position`,
                `asset_id`, `warehouse_id`, `warehouse_area_id`,
                `expression`, `true_result`, `false_result`
            FROM route_composition_temp
        """.trimIndent()
            )
            r.add("DROP TABLE route_composition_temp")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_route_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_data_collection_rule_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_level`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_position`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_route_composition_warehouse_area_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_route_id` ON `route_composition` (`route_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_data_collection_rule_id` ON `route_composition` (`data_collection_rule_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_level` ON `route_composition` (`level`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_position` ON `route_composition` (`position`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_asset_id` ON `route_composition` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_warehouse_id` ON `route_composition` (`warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_route_composition_warehouse_area_id` ON `route_composition` (`warehouse_area_id`);")
            return r
        }
    }
}