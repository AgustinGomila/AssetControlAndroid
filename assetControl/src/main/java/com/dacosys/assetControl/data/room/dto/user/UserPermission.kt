package com.dacosys.assetControl.data.room.dto.user

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.user.UserPermissionObject

class UserPermission(
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.PERMISSION_ID) var permissionId: Long = 0L,
) {

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