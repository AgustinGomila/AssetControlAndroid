package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleTargetEntry

@Entity(
    tableName = DataCollectionRuleTargetEntry.TABLE_NAME,
    indices = [
        Index(
            value = [DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${DataCollectionRuleTargetEntry.TABLE_NAME}_${DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID}"
        ),
        Index(
            value = [DataCollectionRuleTargetEntry.ASSET_ID],
            name = "IDX_${DataCollectionRuleTargetEntry.TABLE_NAME}_${DataCollectionRuleTargetEntry.ASSET_ID}"
        ),
        Index(
            value = [DataCollectionRuleTargetEntry.WAREHOUSE_ID],
            name = "IDX_${DataCollectionRuleTargetEntry.TABLE_NAME}_${DataCollectionRuleTargetEntry.WAREHOUSE_ID}"
        ),
        Index(
            value = [DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${DataCollectionRuleTargetEntry.TABLE_NAME}_${DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID],
            name = "IDX_${DataCollectionRuleTargetEntry.TABLE_NAME}_${DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID}"
        )
    ]
)
data class DataCollectionRuleTargetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ID) val id: Long = 0,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.DATA_COLLECTION_RULE_ID) val dataCollectionRuleId: Long,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.WAREHOUSE_ID) val warehouseId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long?,
    @ColumnInfo(name = DataCollectionRuleTargetEntry.ITEM_CATEGORY_ID) val itemCategoryId: Long?
) {
    constructor(d: DataCollectionRuleTarget) : this(
        id = d.id,
        dataCollectionRuleId = d.dataCollectionRuleId,
        assetId = d.assetId,
        warehouseId = d.warehouseId,
        warehouseAreaId = d.warehouseAreaId,
        itemCategoryId = d.itemCategoryId
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
            r.add("ALTER TABLE data_collection_rule_target RENAME TO data_collection_rule_target_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `data_collection_rule_target`
            (
                `_id`                     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `data_collection_rule_id` INTEGER                           NOT NULL,
                `asset_id`                INTEGER,
                `warehouse_id`            INTEGER,
                `warehouse_area_id`       INTEGER,
                `item_category_id`        INTEGER
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO data_collection_rule_target (
                `data_collection_rule_id`, `asset_id`,
                `warehouse_id`, `warehouse_area_id`, `item_category_id`
                            )
            SELECT
                `data_collection_rule_id`, `asset_id`,
                `warehouse_id`, `warehouse_area_id`, `item_category_id`
            FROM data_collection_rule_target_temp
        """.trimIndent()
            )
            r.add("DROP TABLE data_collection_rule_target_temp")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_target_data_collection_rule_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_target_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_target_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_target_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_target_item_category_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_data_collection_rule_id` ON `data_collection_rule_target` (`data_collection_rule_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_asset_id` ON `data_collection_rule_target` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_warehouse_id` ON `data_collection_rule_target` (`warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_warehouse_area_id` ON `data_collection_rule_target` (`warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_item_category_id` ON `data_collection_rule_target` (`item_category_id`);")
            return r
        }
    }
}