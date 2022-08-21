package com.dacosys.assetControl.model.routes.routeProcessStatus.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteProcessStatusContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteProcessStatusEntry.ROUTE_PROCESS_STATUS_ID,
            RouteProcessStatusEntry.DESCRIPTION
        )
    }

    abstract class RouteProcessStatusEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route_process_status"

            const val ROUTE_PROCESS_STATUS_ID = "_id"
            const val DESCRIPTION = "description"
        }
    }
}
