package com.dacosys.assetControl.data.dataBase.asset

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AssetContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AssetEntry.ASSET_ID,
            AssetEntry.CODE,
            AssetEntry.DESCRIPTION,
            AssetEntry.WAREHOUSE_ID,
            AssetEntry.WAREHOUSE_AREA_ID,
            AssetEntry.ACTIVE,
            AssetEntry.OWNERSHIP_STATUS,
            AssetEntry.STATUS,
            AssetEntry.MISSING_DATE,
            AssetEntry.ITEM_CATEGORY_ID,
            AssetEntry.TRANSFERED,
            AssetEntry.ORIGINAL_WAREHOUSE_ID,
            AssetEntry.ORIGINAL_WAREHOUSE_AREA_ID,
            AssetEntry.LABEL_NUMBER,
            AssetEntry.MANUFACTURER,
            AssetEntry.MODEL,
            AssetEntry.SERIAL_NUMBER,
            AssetEntry.CONDITION,
            AssetEntry.COST_CENTRE_ID,
            AssetEntry.PARENT_ID,
            AssetEntry.EAN,
            AssetEntry.LAST_ASSET_REVIEW_DATE,

            AssetEntry.ITEM_CATEGORY_STR,
            AssetEntry.WAREHOUSE_STR,
            AssetEntry.WAREHOUSE_AREA_STR,
            AssetEntry.ORIGINAL_WAREHOUSE_STR,
            AssetEntry.ORIGINAL_WAREHOUSE_AREA_STR,
            AssetEntry.COST_CENTRE_STR,
            AssetEntry.STATUS_STR,
            AssetEntry.OWNERSHIP_STATUS_STR,
            AssetEntry.CONDITION_STR
        )
    }

    abstract class AssetEntry : BaseColumns {
        /*
        CREATE TABLE "asset" (
        `asset_id` BIGINT NOT NULL,
        `code` NVARCHAR ( 45 ) NOT NULL,
        `description` NVARCHAR ( 255 ) NOT NULL,
        `warehouse_id` BIGINT NOT NULL,
        `warehouse_area_id` BIGINT NOT NULL,
        `active` INT NOT NULL DEFAULT 1,
        `ownership_status` INT NOT NULL DEFAULT 1,
        `status` INT NOT NULL DEFAULT 1,
        `missing_date` DATETIME,
        `item_category_id` BIGINT NOT NULL DEFAULT 0,
        `transfered` INT,
        `original_warehouse_id` BIGINT NOT NULL,
        `original_warehouse_area_id` BIGINT NOT NULL,
        `label_number` INT,
        `manufacturer` NVARCHAR ( 255 ),
        `model` NVARCHAR ( 255 ),
        `serial_number` NVARCHAR ( 255 ),
        `condition` INT,
        `cost_centre_id` BIGINT,
        `parent_id` BIGINT,
        `ean` NVARCHAR ( 100 ),
        `last_asset_review_date` DATETIME,
        PRIMARY KEY(`asset_id`) )
         */

        companion object {
            const val TABLE_NAME = "asset"

            const val ASSET_ID = "_id"
            const val CODE = "code"
            const val DESCRIPTION = "description"
            const val WAREHOUSE_ID = "warehouse_id"
            const val WAREHOUSE_AREA_ID = "warehouse_area_id"
            const val ACTIVE = "active"
            const val OWNERSHIP_STATUS = "ownership_status"
            const val STATUS = "status"
            const val MISSING_DATE = "missing_date"
            const val ITEM_CATEGORY_ID = "item_category_id"
            const val TRANSFERED = "transfered"
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
            const val COST_CENTRE_STR = "cost_centre_str"
            const val STATUS_STR = "status_str"
            const val OWNERSHIP_STATUS_STR = "ownership_status_str"
            const val CONDITION_STR = "condition_str"
        }
    }
}
