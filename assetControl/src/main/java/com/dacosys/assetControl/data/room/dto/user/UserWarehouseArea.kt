package com.dacosys.assetControl.data.room.dto.user

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaObject

class UserWarehouseArea(
    @ColumnInfo(name = Entry.USER_ID) val userId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.SEE) val see: Int = 0,
    @ColumnInfo(name = Entry.MOVE) val move: Int = 0,
    @ColumnInfo(name = Entry.COUNT) val count: Int = 0,
    @ColumnInfo(name = Entry.CHECK) val check: Int = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserWarehouseArea

        if (userId != other.userId) return false
        if (warehouseAreaId != other.warehouseAreaId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + warehouseAreaId.hashCode()
        return result
    }

    object Entry {
        const val TABLE_NAME = "user_warehouse_area"
        const val USER_ID = "user_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val SEE = "see"
        const val MOVE = "move"
        const val COUNT = "count"
        const val CHECK = "check"
    }

    constructor(uwaObj: UserWarehouseAreaObject) : this(
        userId = uwaObj.user_id,
        warehouseAreaId = uwaObj.warehouse_area_id,
        see = uwaObj.see,
        move = uwaObj.move,
        count = uwaObj.count,
        check = uwaObj.check
    )
}
