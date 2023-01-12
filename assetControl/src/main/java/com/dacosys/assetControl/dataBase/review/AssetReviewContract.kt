package com.dacosys.assetControl.dataBase.review

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AssetReviewContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AssetReviewEntry.ASSET_REVIEW_ID,
            AssetReviewEntry.ASSET_REVIEW_DATE,
            AssetReviewEntry.OBS,
            AssetReviewEntry.USER_ID,
            AssetReviewEntry.WAREHOUSE_AREA_ID,
            AssetReviewEntry.WAREHOUSE_ID,
            AssetReviewEntry.MODIFICATION_DATE,
            AssetReviewEntry.COLLECTOR_ASSET_REVIEW_ID,
            AssetReviewEntry.STATUS_ID
        )
    }

    abstract class AssetReviewEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "asset_review"

            const val ASSET_REVIEW_ID = "asset_review_id"
            const val ASSET_REVIEW_DATE = "asset_review_date"
            const val OBS = "obs"
            const val USER_ID = "user_id"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val WAREHOUSE_ID = "warehouse_id"
            const val MODIFICATION_DATE = "modification_date"
            const val COLLECTOR_ASSET_REVIEW_ID = "_id"
            const val STATUS_ID = "status_id"

            const val USER_STR = "user_str"
            const val WAREHOUSE_AREA_STR = "warehouse_area_str"
            const val WAREHOUSE_STR = "warehouse_str"
        }
    }
}
