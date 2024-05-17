package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.route.RouteProcessSteps.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    primaryKeys = [Entry.ROUTE_PROCESS_ID, Entry.LEVEL, Entry.POSITION],
    indices = [
        Index(
            value = [Entry.ROUTE_PROCESS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}"
        ),
        Index(
            value = [Entry.ROUTE_PROCESS_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_CONTENT_ID}"
        ),
        Index(
            value = [Entry.LEVEL],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}"
        ),
        Index(
            value = [Entry.POSITION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.POSITION}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        ),
        Index(value = [Entry.STEP], name = "IDX_${Entry.TABLE_NAME}_${Entry.STEP}")
    ]
)
data class RouteProcessSteps(
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

