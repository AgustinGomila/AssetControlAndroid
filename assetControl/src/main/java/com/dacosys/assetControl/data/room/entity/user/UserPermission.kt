package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.user.UserPermission.Entry
import com.dacosys.assetControl.data.webservice.user.UserPermissionObject

@Entity(
    tableName = Entry.TABLE_NAME,
    primaryKeys = [Entry.USER_ID, Entry.PERMISSION_ID],
    indices = [
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.PERMISSION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.PERMISSION_ID}"
        )
    ]
)
data class UserPermission(
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