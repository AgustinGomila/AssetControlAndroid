package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.asset.AssetEntry

@Entity(
    tableName = AssetEntry.TABLE_NAME, indices = [
        Index(value = [AssetEntry.CODE], name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.CODE}"),
        Index(value = [AssetEntry.DESCRIPTION], name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.DESCRIPTION}"),
        Index(
            value = [AssetEntry.ITEM_CATEGORY_ID],
            name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.ITEM_CATEGORY_ID}"
        ),
        Index(value = [AssetEntry.WAREHOUSE_ID], name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.WAREHOUSE_ID}"),
        Index(
            value = [AssetEntry.WAREHOUSE_AREA_ID],
            name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.WAREHOUSE_AREA_ID}"
        ),
        Index(value = [AssetEntry.SERIAL_NUMBER], name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.SERIAL_NUMBER}"),
        Index(value = [AssetEntry.EAN], name = "IDX_${AssetEntry.TABLE_NAME}_${AssetEntry.EAN}")
    ]
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AssetEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetEntry.CODE) var code: String = "",
    @ColumnInfo(name = AssetEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = AssetEntry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = AssetEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetEntry.ACTIVE, defaultValue = "1") var active: Int = 1,
    @ColumnInfo(name = AssetEntry.OWNERSHIP_STATUS, defaultValue = "1") var ownershipStatus: Int = 1,
    @ColumnInfo(name = AssetEntry.STATUS, defaultValue = "1") var status: Int = 1,
    @ColumnInfo(name = AssetEntry.MISSING_DATE) var missingDate: String? = null,
    @ColumnInfo(name = AssetEntry.ITEM_CATEGORY_ID, defaultValue = "0") var itemCategoryId: Long = 0L,
    @ColumnInfo(name = AssetEntry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_ID) var originalWarehouseId: Long = 0L,
    @ColumnInfo(name = AssetEntry.ORIGINAL_WAREHOUSE_AREA_ID) var originalWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = AssetEntry.LABEL_NUMBER) var labelNumber: Int? = null,
    @ColumnInfo(name = AssetEntry.MANUFACTURER) var manufacturer: String? = null,
    @ColumnInfo(name = AssetEntry.MODEL) var model: String? = null,
    @ColumnInfo(name = AssetEntry.SERIAL_NUMBER) var serialNumber: String? = null,
    @ColumnInfo(name = AssetEntry.CONDITION) var condition: Int? = null,
    @ColumnInfo(name = AssetEntry.COST_CENTRE_ID) var costCentreId: Long? = null,
    @ColumnInfo(name = AssetEntry.PARENT_ID) var parentId: Long? = null,
    @ColumnInfo(name = AssetEntry.EAN) var ean: String? = null,
    @ColumnInfo(name = AssetEntry.LAST_ASSET_REVIEW_DATE) var lastAssetReviewDate: String? = null,
) {
    constructor(a: Asset) : this(
        id = a.id,
        code = a.code,
        description = a.description,
        warehouseId = a.warehouseId,
        warehouseAreaId = a.warehouseAreaId,
        active = a.active,
        ownershipStatus = a.ownershipStatus,
        status = a.status,
        missingDate = a.missingDate,
        itemCategoryId = a.itemCategoryId,
        transferred = a.transferred,
        originalWarehouseId = a.originalWarehouseId,
        originalWarehouseAreaId = a.originalWarehouseAreaId,
        labelNumber = a.labelNumber,
        manufacturer = a.manufacturer,
        model = a.model,
        serialNumber = a.serialNumber,
        condition = a.condition,
        costCentreId = a.costCentreId,
        parentId = a.parentId,
        ean = a.ean,
        lastAssetReviewDate = a.lastAssetReviewDate,
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
            r.add("ALTER TABLE asset RENAME TO asset_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset`
            (
                `_id`                        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `code`                       TEXT                              NOT NULL,
                `description`                TEXT                              NOT NULL,
                `warehouse_id`               INTEGER                           NOT NULL,
                `warehouse_area_id`          INTEGER                           NOT NULL,
                `active`                     INTEGER                           NOT NULL DEFAULT 1,
                `ownership_status`           INTEGER                           NOT NULL DEFAULT 1,
                `status`                     INTEGER                           NOT NULL DEFAULT 1,
                `missing_date`               TEXT,
                `item_category_id`           INTEGER                           NOT NULL DEFAULT 0,
                `transferred`                INTEGER,
                `original_warehouse_id`      INTEGER                           NOT NULL,
                `original_warehouse_area_id` INTEGER                           NOT NULL,
                `label_number`               INTEGER,
                `manufacturer`               TEXT,
                `model`                      TEXT,
                `serial_number`              TEXT,
                `condition`                  INTEGER,
                `cost_centre_id`             INTEGER,
                `parent_id`                  INTEGER,
                `ean`                        TEXT,
                `last_asset_review_date`     TEXT
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO asset (
                _id, code, description, warehouse_id, warehouse_area_id, active,
                ownership_status, status, missing_date, item_category_id, transferred,
                original_warehouse_id, original_warehouse_area_id, label_number,
                manufacturer, model, serial_number, condition, cost_centre_id, parent_id,
                ean, last_asset_review_date
            )
            SELECT
                _id, code, description, warehouse_id, warehouse_area_id, active,
                ownership_status, status, missing_date, item_category_id, transfered,
                original_warehouse_id, original_warehouse_area_id, label_number,
                manufacturer, model, serial_number, condition, cost_centre_id, parent_id,
                ean, last_asset_review_date
            FROM asset_temp
        """.trimIndent()
            )
            r.add("DROP TABLE asset_temp")
            r.add("DROP INDEX IF EXISTS `IDX_asset_code`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_item_category_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_serial_number`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_ean`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_code` ON `asset` (`code`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_description` ON `asset` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_item_category_id` ON `asset` (`item_category_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_warehouse_id` ON `asset` (`warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_warehouse_area_id` ON `asset` (`warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_serial_number` ON `asset` (`serial_number`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_ean` ON `asset` (`ean`);")
            return r
        }
    }
}