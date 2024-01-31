package com.dacosys.assetControl.data.dataBase.route

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object RouteProcessStepsContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            RouteProcessStepsEntry.ROUTE_PROCESS_ID,
            RouteProcessStepsEntry.ROUTE_PROCESS_CONTENT_ID,
            RouteProcessStepsEntry.LEVEL,
            RouteProcessStepsEntry.POSITION,
            RouteProcessStepsEntry.DATA_COLLECTION_ID,
            RouteProcessStepsEntry.STEP

        )
    }

    abstract class RouteProcessStepsEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "route_process_steps"

            /*
            CREATE TABLE [route_process_steps] (
            [route_process_id] bigint NULL ,
            [route_process_content_id] bigint NULL ,
            [level] int NULL ,
            [position] int NULL ,
            [data_collection_id] bigint NULL ,
            [step] int NULL )
             */

            const val ROUTE_PROCESS_ID = "route_process_id"
            const val ROUTE_PROCESS_CONTENT_ID = "route_process_content_id"
            const val LEVEL = "level"
            const val POSITION = "position"
            const val DATA_COLLECTION_ID = "data_collection_id"
            const val STEP = "step"
        }
    }
}
