package com.dacosys.assetControl.data.room.entity.asset

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.enums.asset.AssetCondition
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.asset.OwnershipStatus
import com.dacosys.assetControl.data.room.entity.asset.Asset.Entry
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject
import com.dacosys.assetControl.data.webservice.asset.AssetObject

@Entity(
    tableName = Entry.TABLE_NAME, indices = [
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}"),
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(value = [Entry.ITEM_CATEGORY_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ITEM_CATEGORY_ID}"),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"),
        Index(value = [Entry.WAREHOUSE_AREA_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"),
        Index(value = [Entry.SERIAL_NUMBER], name = "IDX_${Entry.TABLE_NAME}_${Entry.SERIAL_NUMBER}"),
        Index(value = [Entry.EAN], name = "IDX_${Entry.TABLE_NAME}_${Entry.EAN}")
    ]
)
data class Asset(
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
    @Ignore var itemCategoryStr: String = "",
    @Ignore var warehouseStr: String = "",
    @Ignore var warehouseAreaStr: String = "",
    @Ignore var originalWarehouseStr: String = "",
    @Ignore var originalWarehouseAreaStr: String = "",
) : Parcelable {

    fun saveChanges() = AssetRepository().update(this)

    @Ignore
    val assetStatus = AssetStatus.getById(this.status)

    @Ignore
    val ownership = OwnershipStatus.getById(this.ownershipStatus)

    @Ignore
    val assetCondition: AssetCondition = getCondition()

    private fun getCondition(): AssetCondition {
        val c = this.condition
        return if (c == null) AssetCondition.unknown
        else AssetCondition.getById(c)
    }

    constructor() : this(
        id = 0L,
        code = "",
        description = "",
        warehouseId = 0L,
        warehouseAreaId = 0L,
        active = 1,
        ownershipStatus = 1,
        status = 1,
        missingDate = null,
        itemCategoryId = 0L,
        transferred = null,
        originalWarehouseId = 0L,
        originalWarehouseAreaId = 0L,
        labelNumber = null,
        manufacturer = null,
        model = null,
        serialNumber = null,
        condition = null,
        costCentreId = null,
        parentId = null,
        ean = null,
        lastAssetReviewDate = null,
        itemCategoryStr = "",
        warehouseStr = "",
        warehouseAreaStr = "",
        originalWarehouseStr = ""
    )

    constructor(a: AssetObject) : this(
        id = a.asset_id,
        code = a.code,
        description = a.description,
        warehouseId = a.warehouse_id,
        warehouseAreaId = a.warehouse_area_id,
        active = a.active,
        ownershipStatus = a.ownership_status,
        status = a.status,
        missingDate = a.missing_date,
        itemCategoryId = a.item_category_id,
        transferred = 1,
        originalWarehouseId = a.original_warehouse_id,
        originalWarehouseAreaId = a.original_warehouse_area_id,
        labelNumber = a.label_number,
        manufacturer = a.manufacturer,
        model = a.model,
        serialNumber = a.serial_number,
        condition = a.condition,
        costCentreId = a.cost_centre_id,
        parentId = a.parent_id,
        ean = a.ean,
        lastAssetReviewDate = a.last_asset_review_date
    )

    constructor(aco: AssetCollectorObject) : this(
        id = aco.asset_id,
        code = aco.code,
        description = aco.description,
        warehouseId = aco.warehouse_id,
        warehouseAreaId = aco.warehouse_area_id,
        active = aco.active,
        ownershipStatus = aco.ownership_status,
        status = aco.status,
        missingDate = aco.missing_date,
        itemCategoryId = aco.item_category_id,
        transferred = 1,
        originalWarehouseId = aco.original_warehouse_id,
        originalWarehouseAreaId = aco.original_warehouse_area_id,
        labelNumber = aco.label_number,
        manufacturer = "",
        model = "",
        serialNumber = aco.serial_number,
        condition = aco.condition,
        costCentreId = 0, // aco.cost_centre_id,
        parentId = aco.parent_id,
        ean = aco.ean,
        lastAssetReviewDate = aco.last_asset_review_date,
    )

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        code = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        warehouseId = parcel.readLong(),
        warehouseAreaId = parcel.readLong(),
        active = parcel.readInt(),
        ownershipStatus = parcel.readInt(),
        status = parcel.readInt(),
        missingDate = parcel.readString(),
        itemCategoryId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        originalWarehouseId = parcel.readLong(),
        originalWarehouseAreaId = parcel.readLong(),
        labelNumber = parcel.readValue(Int::class.java.classLoader) as? Int,
        manufacturer = parcel.readString(),
        model = parcel.readString(),
        serialNumber = parcel.readString(),
        condition = parcel.readValue(Int::class.java.classLoader) as? Int,
        costCentreId = parcel.readValue(Long::class.java.classLoader) as? Long,
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        ean = parcel.readString(),
        lastAssetReviewDate = parcel.readString(),
        itemCategoryStr = parcel.readString().orEmpty(),
        warehouseStr = parcel.readString().orEmpty(),
        warehouseAreaStr = parcel.readString().orEmpty(),
        originalWarehouseStr = parcel.readString().orEmpty(),
        originalWarehouseAreaStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "asset"
        const val ID = "_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val ACTIVE = "active"
        const val OWNERSHIP_STATUS = "ownership_status"
        const val STATUS = "status"
        const val MISSING_DATE = "missing_date"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val TRANSFERRED = "transferred"
        const val ORIGINAL_WAREHOUSE_ID = "original_warehouse_id"
        const val ORIGINAL_WAREHOUSE_AREA_ID = "original_warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val CONDITION = "condition"
        const val COST_CENTRE_ID = "cost_centre_id"
        const val PARENT_ID = "parent_id"
        const val EAN = "ean"
        const val LAST_ASSET_REVIEW_DATE = "last_asset_review_date"

        const val ITEM_CATEGORY_STR = "item_category_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val ORIGINAL_WAREHOUSE_STR = "orig_warehouse_str"
        const val ORIGINAL_WAREHOUSE_AREA_STR = "orig_warehouse_area_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.id)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeInt(active)
        parcel.writeInt(this.ownershipStatus)
        parcel.writeInt(this.status)
        parcel.writeString(missingDate)
        parcel.writeLong(itemCategoryId)
        parcel.writeValue(transferred)
        parcel.writeLong(originalWarehouseId)
        parcel.writeLong(originalWarehouseAreaId)
        parcel.writeValue(labelNumber)
        parcel.writeString(manufacturer)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeValue(this.condition)
        parcel.writeValue(costCentreId)
        parcel.writeValue(this.parentId)
        parcel.writeString(ean)
        parcel.writeString(lastAssetReviewDate)
        parcel.writeString(itemCategoryStr)
        parcel.writeString(warehouseStr)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(originalWarehouseStr)
        parcel.writeString(originalWarehouseAreaStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Asset

        return this.id == other.id
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }

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