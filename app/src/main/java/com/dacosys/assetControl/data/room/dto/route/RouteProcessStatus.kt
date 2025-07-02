package com.dacosys.assetControl.data.room.dto.route

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus

abstract class RouteProcessStatusEntry {
    companion object {
        const val TABLE_NAME = "route_process_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

class RouteProcessStatus(
    @ColumnInfo(name = RouteProcessStatusEntry.ID) val id: Int = 0,
    @ColumnInfo(name = RouteProcessStatusEntry.DESCRIPTION) val description: String = ""
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteProcessStatus

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }

    constructor(status: RouteProcessStatus) : this(
        id = status.id,
        description = status.description
    )
}

