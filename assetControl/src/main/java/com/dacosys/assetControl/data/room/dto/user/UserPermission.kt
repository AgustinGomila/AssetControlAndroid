package com.dacosys.assetControl.data.room.dto.user

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.user.UserPermissionObject

class UserPermission(
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.PERMISSION_ID) var permissionId: Long = 0L,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserPermission

        if (userId != other.userId) return false
        if (permissionId != other.permissionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + permissionId.hashCode()
        return result
    }

    object Entry {
        const val TABLE_NAME = "user_permission"
        const val USER_ID = "user_id"
        const val PERMISSION_ID = "permission_id"
    }

    constructor(upObj: UserPermissionObject) : this(
        userId = upObj.user_id,
        permissionId = upObj.permission_id
    )
}