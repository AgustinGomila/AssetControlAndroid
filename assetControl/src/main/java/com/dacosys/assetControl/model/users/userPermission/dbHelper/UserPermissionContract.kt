package com.dacosys.assetControl.model.users.userPermission.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object UserPermissionContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            UserPermissionEntry.USER_ID,
            UserPermissionEntry.PERMISSION_ID
        )
    }

    abstract class UserPermissionEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "user_permission"

            const val PERMISSION_ID = "permission_id"
            const val USER_ID = "user_id"
        }
    }
}
