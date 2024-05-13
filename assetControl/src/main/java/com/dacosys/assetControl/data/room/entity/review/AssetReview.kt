package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.review.AssetReview.Entry

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
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        )
    ]
)
data class AssetReview(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.ASSET_REVIEW_DATE) val assetReviewDate: String,
    @ColumnInfo(name = Entry.OBS) val obs: String?,
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long,
    @ColumnInfo(name = Entry.MODIFICATION_DATE) val modificationDate: String,
    @ColumnInfo(name = Entry.STATUS_ID) val statusId: Int
) {
    object Entry {
        const val TABLE_NAME = "asset_review"
        const val ID = "_id"
        const val ASSET_REVIEW_DATE = "asset_review_date"
        const val OBS = "obs"
        const val USER_ID = "user_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val MODIFICATION_DATE = "modification_date"
        const val STATUS_ID = "status_id"
    }
}
