package com.dacosys.assetControl.data.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.route.RouteProcess.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(value = [Entry.ROUTE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_ID}"),
        Index(value = [Entry.ROUTE_PROCESS_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}")
    ]
)
data class RouteProcess(
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.ROUTE_ID) val routeId: Long,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_DATE) val routeProcessDate: Date,
    @ColumnInfo(name = Entry.COMPLETED) val completed: Int,
    @ColumnInfo(name = Entry.TRANSFERRED) val transferred: Int?,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) val transferredDate: Date?,
    @PrimaryKey @ColumnInfo(name = Entry.ID) val id: Long
) {
    object Entry {
        const val TABLE_NAME = "route_process"
        const val USER_ID = "user_id"
        const val ROUTE_ID = "route_id"
        const val ROUTE_PROCESS_DATE = "route_process_date"
        const val COMPLETED = "completed"
        const val TRANSFERRED = "transfered"
        const val TRANSFERRED_DATE = "transfered_date"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val ID = "_id"
    }
}
