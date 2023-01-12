package com.dacosys.assetControl.dataBase.manteinance

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object ManteinanceStatusContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            ManteinanceStatusEntry.MANTEINANCE_STATUS_ID,
            ManteinanceStatusEntry.DESCRIPTION
        )
    }

    abstract class ManteinanceStatusEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "manteinance_status"

            const val MANTEINANCE_STATUS_ID = "_id"
            const val DESCRIPTION = "description"
        }
    }
}
