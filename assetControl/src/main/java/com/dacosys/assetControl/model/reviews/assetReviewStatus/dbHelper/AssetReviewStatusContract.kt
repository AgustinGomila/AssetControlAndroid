package com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AssetReviewStatusContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AssetReviewStatusEntry.STATUS_ID,
            AssetReviewStatusEntry.DESCRIPTION
        )
    }

    abstract class AssetReviewStatusEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "status"

            const val STATUS_ID = "_id"
            const val DESCRIPTION = "description"
        }
    }
}
