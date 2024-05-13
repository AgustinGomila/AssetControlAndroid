package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.user.UserPermission.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
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
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.PERMISSION_ID) val permissionId: Long
) {
    object Entry {
        const val TABLE_NAME = "user_permission"
        const val USER_ID = "user_id"
        const val PERMISSION_ID = "permission_id"
    }
}