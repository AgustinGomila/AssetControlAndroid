package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo

class RouteProcessSteps(
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) val routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_CONTENT_ID) val routeProcessContentId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) val position: Int = 0,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) val dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.STEP) val step: Int = 0,
) {
    object Entry {
        const val TABLE_NAME = "route_process_steps"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val ROUTE_PROCESS_CONTENT_ID = "route_process_content_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val STEP = "step"
    }
}

