package com.dacosys.assetControl.dataBase.manteinance

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object ManteinanceTypeGroupContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            ManteinanceTypeGroupEntry.MANTEINANCE_TYPE_GROUP_ID,
            ManteinanceTypeGroupEntry.DESCRIPTION,
            ManteinanceTypeGroupEntry.ACTIVE
        )
    }

    abstract class ManteinanceTypeGroupEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "manteinance_type_group"

            const val MANTEINANCE_TYPE_GROUP_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
        }
    }
}
