package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.ASSET_REVIEW_ID) val assetReviewId: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long?,
    @ColumnInfo(name = Entry.CODE) val code: String,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.QTY) val qty: Double?,
    @ColumnInfo(name = Entry.CONTENT_STATUS_ID) val contentStatusId: Int,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) val originWarehouseAreaId: Long
) {
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
    }
}
