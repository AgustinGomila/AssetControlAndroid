package com.dacosys.assetControl.data.room.entity.movement

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent.Entry
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.WAREHOUSE_MOVEMENT_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_MOVEMENT_ID}"),
        Index(value = [Entry.ASSET_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"),
        Index(value = [Entry.CODE], name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}")
    ]
)
data class WarehouseMovementContent(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long = 0L, // NOT NULL in Migration 1.2
    @ColumnInfo(name = Entry.CODE) var code: String = "",
    @ColumnInfo(name = Entry.QTY) var qty: Double? = null,
    @Ignore var contentStatusId: Int = 0,
    @Ignore var assetStatusId: Int = 0,
    @Ignore var description: String = "",
    @Ignore var warehouseAreaId: Long = 0L,
    @Ignore var labelNumber: Int = 0,
    @Ignore var parentId: Long? = null,
    @Ignore var warehouseAreaStr: String = "",
    @Ignore var warehouseStr: String = "",
    @Ignore var itemCategoryId: Long = 0L,
    @Ignore var itemCategoryStr: String = "",
    @Ignore var ownershipStatusId: Int = 0,
    @Ignore var manufacturer: String = "",
    @Ignore var model: String = "",
    @Ignore var serialNumber: String = "",
    @Ignore var ean: String = "",
) : Parcelable {

    @Ignore
    private var assetRead: Boolean = false

    @Ignore
    private lateinit var mAsset: Asset

    fun asset(): Asset {
        if (!assetRead) {
            mAsset = AssetRepository().selectById(assetId) ?: Asset()
        }
        return mAsset
    }

    constructor(movementId: Long, id: Long, asset: Asset, contentStatusId: Int) : this(
        warehouseMovementId = movementId,
        id = id,
        assetId = asset.id,
        code = asset.code,
        qty = 1.0,
        contentStatusId = contentStatusId,
        assetStatusId = asset.status,
        description = asset.description,
        warehouseAreaId = asset.warehouseAreaId,
        labelNumber = asset.labelNumber ?: 0,
        parentId = asset.parentId,
        warehouseAreaStr = asset.warehouseAreaStr,
        warehouseStr = asset.warehouseStr,
        itemCategoryId = asset.itemCategoryId,
        itemCategoryStr = asset.itemCategoryStr,
        ownershipStatusId = asset.ownershipStatus,
        manufacturer = asset.manufacturer.orEmpty(),
        model = asset.model.orEmpty(),
        serialNumber = asset.serialNumber.orEmpty(),
        ean = asset.ean.orEmpty()
    )

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        warehouseMovementId = parcel.readLong(),
        assetId = parcel.readLong(),
        code = parcel.readString().orEmpty(),
        qty = parcel.readValue(Double::class.java.classLoader) as? Double,
        contentStatusId = parcel.readInt(),
        assetStatusId = parcel.readInt(),
        description = parcel.readString().orEmpty(),
        warehouseAreaId = parcel.readLong(),
        labelNumber = parcel.readInt(),
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaStr = parcel.readString().orEmpty(),
        warehouseStr = parcel.readString().orEmpty(),
        itemCategoryId = parcel.readLong(),
        itemCategoryStr = parcel.readString().orEmpty(),
        ownershipStatusId = parcel.readInt(),
        manufacturer = parcel.readString().orEmpty(),
        model = parcel.readString().orEmpty(),
        serialNumber = parcel.readString().orEmpty(),
        ean = parcel.readString().orEmpty()
    )

    constructor(arc: AssetReviewContent) : this(
        assetId = arc.assetId,
        code = arc.code,
        qty = arc.qty
    )

    constructor(tContent: TempMovementContent) : this(
        id = tContent.id,
        warehouseMovementId = tContent.warehouseMovementId,
        assetId = tContent.assetId,
        code = tContent.code,
        qty = tContent.qty,
        contentStatusId = tContent.contentStatus,
        assetStatusId = tContent.status,
        description = tContent.description,
        warehouseAreaId = tContent.warehouseAreaId,
        labelNumber = tContent.labelNumber ?: 0,
        parentId = tContent.parentId,
        warehouseStr = tContent.warehouseStr,
        warehouseAreaStr = tContent.warehouseAreaStr,
        itemCategoryId = tContent.itemCategoryId,
        itemCategoryStr = tContent.itemCategoryStr,
        ownershipStatusId = tContent.ownershipStatus,
        manufacturer = tContent.manufacturer.orEmpty(),
        model = tContent.model.orEmpty(),
        serialNumber = tContent.serialNumber.orEmpty(),
        ean = tContent.ean.orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "warehouse_movement_content"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val QTY = "qty"

        const val DESCRIPTION = "description"
        const val ASSET_STATUS_ID = "asset_status_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val LABEL_NUMBER = "label_number"
        const val PARENT_ID = "parent_id"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val WAREHOUSE_STR = "warehouse_str"
        const val ITEM_CATEGORY_ID = "item_category_id"
        const val ITEM_CATEGORY_STR = "item_category_str"
        const val OWNERSHIP_STATUS_ID = "ownership_status_id"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val SERIAL_NUMBER = "serial_number"
        const val EAN = "ean"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(warehouseMovementId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeFloat(qty?.toFloat() ?: 0F)
        parcel.writeInt(contentStatusId)
        parcel.writeInt(assetStatusId)
        parcel.writeString(description)
        parcel.writeLong(warehouseAreaId)
        parcel.writeInt(labelNumber)
        parcel.writeValue(parentId)
        parcel.writeString(warehouseAreaStr)
        parcel.writeString(warehouseStr)
        parcel.writeLong(itemCategoryId)
        parcel.writeString(itemCategoryStr)
        parcel.writeInt(ownershipStatusId)
        parcel.writeString(manufacturer)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeString(ean)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovementContent> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovementContent {
            return WarehouseMovementContent(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovementContent?> {
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
            r.add("ALTER TABLE warehouse_movement_content RENAME TO warehouse_movement_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_movement_content`
            (
                `_id`                   INTEGER NOT NULL,
                `warehouse_movement_id` INTEGER NOT NULL,
                `asset_id`              INTEGER NOT NULL,
                `code`                  TEXT    NOT NULL,
                `qty`                   REAL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_movement_content (
                `_id`, `warehouse_movement_id`,
                `asset_id`, `code`, `qty`
            )
            SELECT
                `_id`, `warehouse_movement_id`,
                `asset_id`, `code`, `qty`
            FROM warehouse_movement_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_movement_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_warehouse_movement_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_content_code`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_warehouse_movement_id` ON `warehouse_movement_content` (`warehouse_movement_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_asset_id` ON `warehouse_movement_content` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_code` ON `warehouse_movement_content` (`code`);")
            return r
        }
    }
}