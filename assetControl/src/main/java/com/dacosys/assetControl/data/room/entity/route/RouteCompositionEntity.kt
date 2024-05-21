package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.dto.route.RouteComposition
import com.dacosys.assetControl.data.room.dto.route.RouteComposition.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    primaryKeys = [Entry.ROUTE_ID, Entry.LEVEL, Entry.POSITION],
    indices = [
        Index(
            value = [Entry.ROUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_ID}"
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
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        )
    ]
)
data class RouteCompositionEntity(
    @ColumnInfo(name = Entry.ROUTE_ID) val routeId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) val position: Int = 0,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long? = null,
    @ColumnInfo(name = Entry.EXPRESSION) val expression: String? = null,
    @ColumnInfo(name = Entry.TRUE_RESULT) val trueResult: Int = 0,
    @ColumnInfo(name = Entry.FALSE_RESULT) val falseResult: Int = 0
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