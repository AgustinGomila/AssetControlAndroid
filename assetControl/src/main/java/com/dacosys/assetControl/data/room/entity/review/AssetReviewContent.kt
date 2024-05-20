package com.dacosys.assetControl.data.room.entity.review

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent.Entry


@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ASSET_REVIEW_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_REVIEW_ID}"
        ),
        Index(
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.CODE],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.CODE}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        ),
        Index(
            value = [Entry.ORIGIN_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_AREA_ID}"
        )
    ]
)
data class AssetReviewContent(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_REVIEW_ID) var assetReviewId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long = 0L, // NOT NULL in Migration 1.2
    @ColumnInfo(name = Entry.CODE) var code: String = "",
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.QTY) var qty: Double? = null,
    @ColumnInfo(name = Entry.CONTENT_STATUS_ID) var contentStatusId: Int = 0,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @Ignore var assetStatusId: Int = 0,
    @Ignore var labelNumber: Int = 0,
    @Ignore var parentId: Long? = null,
    @Ignore var warehouseAreaId: Long = 0L,
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
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetReviewId = parcel.readLong(),
        assetId = parcel.readLong(),
        code = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        qty = parcel.readValue(Double::class.java.classLoader) as? Double,
        contentStatusId = parcel.readInt(),
        originWarehouseAreaId = parcel.readLong(),
        assetStatusId = parcel.readInt(),
        labelNumber = parcel.readInt(),
        parentId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaId = parcel.readLong(),
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

    constructor(tContent: TempReviewContent) : this(
        id = tContent.assetReviewContentId,
        assetReviewId = tContent.assetReviewId,
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

    constructor(
        assetReviewId: Long,
        id: Long,
        asset: Asset,
        qty: Double?,
        contentStatusId: Int,
        originWarehouseAreaId: Long
    ) : this(
        assetReviewId = assetReviewId,
        id = id,
        assetId = asset.id,
        code = asset.code,
        description = asset.description,
        assetStatusId = asset.status,
        labelNumber = asset.labelNumber ?: 0,
        parentId = asset.parentId,
        warehouseAreaId = asset.warehouseAreaId,
        warehouseAreaStr = asset.warehouseAreaStr,
        warehouseStr = asset.warehouseStr,
        itemCategoryId = asset.itemCategoryId,
        itemCategoryStr = asset.itemCategoryStr,
        ownershipStatusId = asset.status,
        manufacturer = asset.manufacturer.orEmpty(),
        model = asset.model.orEmpty(),
        serialNumber = asset.serialNumber.orEmpty(),
        ean = asset.ean.orEmpty(),
        qty = qty,
        contentStatusId = contentStatusId,
        originWarehouseAreaId = originWarehouseAreaId,
    )

    object Entry {
        const val TABLE_NAME = "asset_review_content"
        const val ID = "_id"
        const val ASSET_REVIEW_ID = "asset_review_id"
        const val ASSET_ID = "asset_id"
        const val CODE = "code"
        const val DESCRIPTION = "description"
        const val QTY = "qty"
        const val CONTENT_STATUS_ID = "content_status_id"
        const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"

        const val LABEL_NUMBER = "label_number"
        const val PARENT_ID = "parent_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
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
        parcel.writeLong(assetReviewId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeFloat(qty?.toFloat() ?: 0F)
        parcel.writeInt(contentStatusId)
        parcel.writeLong(originWarehouseAreaId)
        parcel.writeInt(assetStatusId)
        parcel.writeInt(labelNumber)
        parcel.writeValue(parentId)
        parcel.writeLong(warehouseAreaId)
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

    companion object CREATOR : Parcelable.Creator<AssetReviewContent> {
        override fun createFromParcel(parcel: Parcel): AssetReviewContent {
            return AssetReviewContent(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContent?> {
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
            r.add("ALTER TABLE asset_review_content RENAME TO asset_review_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset_review_content`
            (
                `_id`                      INTEGER NOT NULL,
                `asset_review_id`          INTEGER NOT NULL,
                `asset_id`                 INTEGER NOT NULL,
                `code`                     TEXT    NOT NULL,
                `description`              TEXT    NOT NULL,
                `qty`                      REAL,
                `content_status_id`        INTEGER NOT NULL,
                `origin_warehouse_area_id` INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO asset_review_content (
                _id, asset_review_id,
                asset_id, code, description,
                qty, content_status_id, origin_warehouse_area_id
            )
            SELECT
                asset_review_content_id, asset_review_id,
                asset_id, code, description,
                qty, content_status_id, origin_warehouse_area_id
            FROM asset_review_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE asset_review_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_content_asset_review_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_content_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_content_code`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_content_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_content_origin_warehouse_area_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_asset_review_id` ON `asset_review_content` (`asset_review_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_asset_id` ON `asset_review_content` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_code` ON `asset_review_content` (`code`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_description` ON `asset_review_content` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_origin_warehouse_area_id` ON `asset_review_content` (`origin_warehouse_area_id`);")
            return r
        }
    }
}