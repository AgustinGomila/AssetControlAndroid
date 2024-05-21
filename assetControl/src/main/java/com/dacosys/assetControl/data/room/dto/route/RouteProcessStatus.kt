package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus

class RouteProcessStatus(
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {

    override fun toString(): String {
        return description
    }

    object Entry {
        const val TABLE_NAME = "route_process_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }

    constructor(status: RouteProcessStatus) : this(
        id = status.id,
        description = status.description
    )
}

