package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.review.AssetReview
import com.dacosys.assetControl.data.room.dto.review.AssetReview.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"
        )
    ]
)
data class AssetReviewEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_REVIEW_DATE) var assetReviewDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.MODIFICATION_DATE) var modificationDate: Date = Date(),
    @ColumnInfo(name = Entry.STATUS_ID) var statusId: Int = 0,
) {
    constructor(a: AssetReview) : this(
        id = a.id,
        assetReviewDate = a.assetReviewDate,
        obs = a.obs,
        userId = a.userId,
        warehouseAreaId = a.warehouseAreaId,
        warehouseId = a.warehouseId,
        modificationDate = a.modificationDate,
        statusId = a.statusId,
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
            r.add("ALTER TABLE asset_review RENAME TO asset_review_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset_review`
            (
                `_id`               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `asset_review_date` INTEGER NOT NULL,
                `obs`               TEXT,
                `user_id`           INTEGER NOT NULL,
                `warehouse_area_id` INTEGER NOT NULL,
                `warehouse_id`      INTEGER NOT NULL,
                `modification_date` INTEGER NOT NULL,
                `status_id`         INTEGER NOT NULL
            );
                """.trimIndent()
            )
            r.add(
                """
            INSERT INTO asset_review (
                _id, asset_review_date, obs,
                status_id, user_id, warehouse_area_id,
                modification_date, warehouse_id
                )
            SELECT
                _id, strftime('%s', asset_review_date), obs,
                status_id, user_id, warehouse_area_id,
                strftime('%s', modification_date), warehouse_id
            FROM asset_review_temp"""
            )
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_review__id`;")
            r.add("DROP TABLE asset_review_temp;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_user_id` ON `asset_review` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_warehouse_area_id` ON `asset_review` (`warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_review_warehouse_id` ON `asset_review` (`warehouse_id`);")
            return r
        }
    }
}