package com.dacosys.assetControl.dataBase.review

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AssetReviewContentContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AssetReviewContentEntry.ASSET_REVIEW_ID,
            AssetReviewContentEntry.ASSET_REVIEW_CONTENT_ID,
            AssetReviewContentEntry.ASSET_ID,
            AssetReviewContentEntry.CODE,
            AssetReviewContentEntry.DESCRIPTION,
            AssetReviewContentEntry.QTY,
            AssetReviewContentEntry.CONTENT_STATUS_ID,
            AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID
        )
    }

    abstract class AssetReviewContentEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "asset_review_content"

            const val ASSET_REVIEW_ID = "asset_review_id"
            const val ASSET_REVIEW_CONTENT_ID = "asset_review_content_id"
            const val ASSET_ID = "asset_id"
            const val CODE = "code"
            const val DESCRIPTION = "description"
            const val QTY = "qty"
            const val CONTENT_STATUS_ID = "content_status_id"
            const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"
        }
    }
}
