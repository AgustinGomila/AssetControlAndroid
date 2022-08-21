package com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object ManteinanceTypeContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            ManteinanceTypeEntry.MANTEINANCE_TYPE_ID,
            ManteinanceTypeEntry.DESCRIPTION,
            ManteinanceTypeEntry.ACTIVE,
            ManteinanceTypeEntry.MANTEINANCE_TYPE_GROUP_ID
        )
    }

    abstract class ManteinanceTypeEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "manteinance_type"

            const val MANTEINANCE_TYPE_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val MANTEINANCE_TYPE_GROUP_ID = "manteinance_type_group_id"
        }
    }
}
