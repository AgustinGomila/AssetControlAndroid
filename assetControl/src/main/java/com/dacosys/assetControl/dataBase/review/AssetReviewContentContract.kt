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

    fun getAllTempColumns(): Array<String> {
        return arrayOf(
            AssetReviewContentEntry.ASSET_REVIEW_ID,
            AssetReviewContentEntry.ASSET_REVIEW_CONTENT_ID,
            AssetReviewContentEntry.ASSET_ID,
            AssetReviewContentEntry.CODE,
            AssetReviewContentEntry.DESCRIPTION,
            AssetReviewContentEntry.QTY,
            AssetReviewContentEntry.CONTENT_STATUS_ID,
            AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID,
            AssetReviewContentEntry.WAREHOUSE_AREA_ID,
            AssetReviewContentEntry.OWNERSHIP_STATUS,
            AssetReviewContentEntry.STATUS,
            AssetReviewContentEntry.ITEM_CATEGORY_ID,
            AssetReviewContentEntry.LABEL_NUMBER,
            AssetReviewContentEntry.MANUFACTURER,
            AssetReviewContentEntry.MODEL,
            AssetReviewContentEntry.SERIAL_NUMBER,
            AssetReviewContentEntry.PARENT_ID,
            AssetReviewContentEntry.EAN,
            AssetReviewContentEntry.ITEM_CATEGORY_STR,
            AssetReviewContentEntry.WAREHOUSE_STR,
            AssetReviewContentEntry.WAREHOUSE_AREA_STR
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

            // Tabla temporal
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val OWNERSHIP_STATUS = "ownership_status"
            const val STATUS = "status"
            const val ITEM_CATEGORY_ID = "item_category_id"
            const val LABEL_NUMBER = "label_number"
            const val MANUFACTURER = "manufacturer"
            const val MODEL = "model"
            const val SERIAL_NUMBER = "serial_number"
            const val PARENT_ID = "parent_id"
            const val EAN = "ean"
            const val ITEM_CATEGORY_STR = "item_category_str"
            const val WAREHOUSE_STR = "warehouse_str"
            const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        }
    }
}
