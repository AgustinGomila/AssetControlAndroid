package com.dacosys.assetControl.dataBase.route

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteProcessContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteProcessEntry.USER_ID,
            RouteProcessEntry.ROUTE_ID,
            RouteProcessEntry.ROUTE_PROCESS_DATE,
            RouteProcessEntry.COMPLETED,
            RouteProcessEntry.TRANSFERED,
            RouteProcessEntry.TRANSFERED_DATE,
            RouteProcessEntry.ROUTE_PROCESS_ID,
            RouteProcessEntry.COLLECTOR_ROUTE_PROCESS_ID
        )
    }

    abstract class RouteProcessEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route_process"

            /*
            CREATE TABLE "route_process" (
            `user_id` bigint NOT NULL,
            `route_id` bigint NOT NULL,
            `route_process_date` datetime NOT NULL,
            `completed` int NOT NULL,
            `transfered` int,
            `transfered_date` datetime,
            `route_process_id` bigint,
            `_id` bigint NOT NULL,
            CONSTRAINT `PK__route_process__00000000000007FD` PRIMARY KEY(`_id`) )
             */

            const val USER_ID = "user_id"
            const val ROUTE_ID = "route_id"
            const val ROUTE_PROCESS_DATE = "route_process_date"
            const val COMPLETED = "completed"
            const val TRANSFERED = "transfered"
            const val TRANSFERED_DATE = "transfered_date"
            const val ROUTE_PROCESS_ID = "route_process_id"
            const val COLLECTOR_ROUTE_PROCESS_ID = "_id"

            const val USER_STR = "user_str"
            const val ROUTE_STR = "route_str"
        }
    }
}
