package com.dacosys.assetControl.dataBase.barcode

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object BarcodeLabelTargetContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            BarcodeLabelTargetEntry.BARCODE_LABEL_TARGET_ID,
            BarcodeLabelTargetEntry.DESCRIPTION
        )
    }

    abstract class BarcodeLabelTargetEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "barcode_label_target"

            const val BARCODE_LABEL_TARGET_ID = "_id"
            const val DESCRIPTION = "description"
        }
    }
}
