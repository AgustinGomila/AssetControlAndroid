package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo

abstract class RouteProcessStepsEntry {
    companion object {
        const val TABLE_NAME = "route_process_steps"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val ROUTE_PROCESS_CONTENT_ID = "route_process_content_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val STEP = "step"
    }
}

class RouteProcessSteps(
    @ColumnInfo(name = RouteProcessStepsEntry.ROUTE_PROCESS_ID) val routeProcessId: Long = 0L,
    @ColumnInfo(name = RouteProcessStepsEntry.ROUTE_PROCESS_CONTENT_ID) val routeProcessContentId: Long = 0L,
    @ColumnInfo(name = RouteProcessStepsEntry.LEVEL) val level: Int = 0,
    @ColumnInfo(name = RouteProcessStepsEntry.POSITION) val position: Int = 0,
    @ColumnInfo(name = RouteProcessStepsEntry.DATA_COLLECTION_ID) val dataCollectionId: Long? = null,
    @ColumnInfo(name = RouteProcessStepsEntry.STEP) val step: Int = 0,
) {

    override fun hashCode(): Int {
        var result = routeProcessId.hashCode()
        result = 31 * result + level
        result = 31 * result + position
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteProcessSteps

        if (routeProcessId != other.routeProcessId) return false
        if (level != other.level) return false
        if (position != other.position) return false

        return true
    }
}

