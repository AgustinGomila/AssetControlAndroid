package com.example.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.dto.review.AssetReviewContentEntry

@Entity(
    tableName = AssetReviewContentEntry.TABLE_NAME,
    indices = [
        Index(
            value = [AssetReviewContentEntry.ASSET_REVIEW_ID],
            name = "IDX_${AssetReviewContentEntry.TABLE_NAME}_${AssetReviewContentEntry.ASSET_REVIEW_ID}"
        ),
        Index(
            value = [AssetReviewContentEntry.ASSET_ID],
            name = "IDX_${AssetReviewContentEntry.TABLE_NAME}_${AssetReviewContentEntry.ASSET_ID}"
        ),
        Index(
            value = [AssetReviewContentEntry.CODE],
            name = "IDX_${AssetReviewContentEntry.TABLE_NAME}_${AssetReviewContentEntry.CODE}"
        ),
        Index(
            value = [AssetReviewContentEntry.DESCRIPTION],
            name = "IDX_${AssetReviewContentEntry.TABLE_NAME}_${AssetReviewContentEntry.DESCRIPTION}"
        ),
        Index(
            value = [AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID],
            name = "IDX_${AssetReviewContentEntry.TABLE_NAME}_${AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID}"
        )
    ]
)
data class AssetReviewContentEntity(
    @PrimaryKey
    @ColumnInfo(name = AssetReviewContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ASSET_REVIEW_ID) var assetReviewId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = AssetReviewContentEntry.CODE) var code: String = "",
    @ColumnInfo(name = AssetReviewContentEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = AssetReviewContentEntry.QTY) var qty: Double? = null,
    @ColumnInfo(name = AssetReviewContentEntry.CONTENT_STATUS_ID) var contentStatusId: Int = 0,
    @ColumnInfo(name = AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
) {
    constructor(a: AssetReviewContent) : this(
        id = a.id,
        assetReviewId = a.assetReviewId,
        assetId = a.assetId,
        code = a.code,
        description = a.description,
        qty = a.qty,
        contentStatusId = a.contentStatusId,
        originWarehouseAreaId = a.originWarehouseAreaId
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
            r.add("ALTER TABLE asset_review_content RENAME TO asset_review_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset_review_content`
            (
                `_id`                      INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `asset_review_id`          INTEGER NOT NULL,
                `asset_id`                 INTEGER NOT NULL,
                `code`                     TEXT    NOT NULL,
                `description`              TEXT    NOT NULL,
                `qty`                      REAL,
                `content_status_id`        INTEGER NOT NULL,
                `origin_warehouse_area_id` INTEGER NOT NULL
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