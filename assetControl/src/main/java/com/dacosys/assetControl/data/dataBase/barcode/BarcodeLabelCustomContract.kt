package com.dacosys.assetControl.data.dataBase.barcode

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object BarcodeLabelCustomContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            BarcodeLabelCustomEntry.BARCODE_LABEL_CUSTOM_ID,
            BarcodeLabelCustomEntry.DESCRIPTION,
            BarcodeLabelCustomEntry.ACTIVE,
            BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID,
            BarcodeLabelCustomEntry.TEMPLATE
        )
    }

    abstract class BarcodeLabelCustomEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "barcode_label_custom"

            const val BARCODE_LABEL_CUSTOM_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val BARCODE_LABEL_TARGET_ID = "barcode_label_target_id"
            const val TEMPLATE = "template"
        }
    }
}
