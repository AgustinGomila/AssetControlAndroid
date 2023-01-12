package com.dacosys.assetControl.dataBase.route

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteEntry.ROUTE_ID,
            RouteEntry.DESCRIPTION,
            RouteEntry.ACTIVE
        )
    }

    abstract class RouteEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route"

            const val ROUTE_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
        }
    }
}
