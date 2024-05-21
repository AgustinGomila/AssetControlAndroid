package com.dacosys.assetControl.data.room.entity.asset

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.asset.Asset.Entry

@Entity(
    tableName = Entry.TABLE_NAME, indices = [
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}"),
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(
            value = [Entry.ITEM_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ITEM_CATEGORY_ID}"
        ),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(value = [Entry.SERIAL_NUMBER], name = "IDX_${Entry.TABLE_NAME}_${Entry.SERIAL_NUMBER}"),
        Index(value = [Entry.EAN], name = "IDX_${Entry.TABLE_NAME}_${Entry.EAN}")
    ]
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.CODE) var code: String = "",
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.ACTIVE, defaultValue = "1") var active: Int = 1,
    @ColumnInfo(name = Entry.OWNERSHIP_STATUS, defaultValue = "1") var ownershipStatus: Int = 1,
    @ColumnInfo(name = Entry.STATUS, defaultValue = "1") var status: Int = 1,
    @ColumnInfo(name = Entry.MISSING_DATE) var missingDate: String? = null,
    @ColumnInfo(name = Entry.ITEM_CATEGORY_ID, defaultValue = "0") var itemCategoryId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.ORIGINAL_WAREHOUSE_ID) var originalWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGINAL_WAREHOUSE_AREA_ID) var originalWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.LABEL_NUMBER) var labelNumber: Int? = null,
    @ColumnInfo(name = Entry.MANUFACTURER) var manufacturer: String? = null,
    @ColumnInfo(name = Entry.MODEL) var model: String? = null,
    @ColumnInfo(name = Entry.SERIAL_NUMBER) var serialNumber: String? = null,
    @ColumnInfo(name = Entry.CONDITION) var condition: Int? = null,
    @ColumnInfo(name = Entry.COST_CENTRE_ID) var costCentreId: Long? = null,
    @ColumnInfo(name = Entry.PARENT_ID) var parentId: Long? = null,
    @ColumnInfo(name = Entry.EAN) var ean: String? = null,
    @ColumnInfo(name = Entry.LAST_ASSET_REVIEW_DATE) var lastAssetReviewDate: String? = null,
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