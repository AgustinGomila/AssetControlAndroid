package com.dacosys.assetControl.dataBase.manteinance

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AssetManteinanceContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AssetManteinanceEntry.COLLECTOR_ASSET_MANTEINANCE_ID,
            AssetManteinanceEntry.ASSET_MANTEINANCE_ID,
            AssetManteinanceEntry.MANTEINANCE_TYPE_ID,
            AssetManteinanceEntry.MANTEINANCE_STATUS_ID,
            AssetManteinanceEntry.ASSET_ID,
            AssetManteinanceEntry.OBSERVATIONS,
            AssetManteinanceEntry.TRANSFERRED
        )
    }

    abstract class AssetManteinanceEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "asset_manteinance_collector"

            const val COLLECTOR_ASSET_MANTEINANCE_ID = "_id"
            const val ASSET_MANTEINANCE_ID = "asset_manteinance_id"
            const val MANTEINANCE_TYPE_ID = "manteinance_type_id"
            const val MANTEINANCE_STATUS_ID = "manteinance_status_id"
            const val ASSET_ID = "asset_id"
            const val OBSERVATIONS = "observations"
            const val TRANSFERRED = "transfered"
        }
    }
}
