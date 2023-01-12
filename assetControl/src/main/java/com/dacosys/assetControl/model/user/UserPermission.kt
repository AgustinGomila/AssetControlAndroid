package com.dacosys.assetControl.model.user

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.UserPermissionEntry.Companion.PERMISSION_ID
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.UserPermissionEntry.Companion.USER_ID
import com.dacosys.assetControl.dataBase.user.UserPermissionDbHelper
import com.dacosys.assetControl.model.user.permission.PermissionEntry

class UserPermission : Parcelable {
    constructor(
        userId: Long,
        permissionId: Long,
    ) {
        this.userId = userId
        this.permissionId = permissionId
    }

    override fun toString(): String {
        return "$userId,$permissionId"
    }

    var userId: Long = 0
    var permissionId: Long = 0

    var permissionEntry: PermissionEntry? = null
        get() = if (field != null)
            PermissionEntry.getById(permissionId)
        else null
        set(value) {
            if (value != null) {
                permissionId = value.id
            }
            field = value
        }

    constructor(parcel: Parcel) {
        userId = parcel.readLong()
        permissionId = parcel.readLong()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(USER_ID, userId)
        values.put(PERMISSION_ID, permissionId)
        return values
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is UserPermission) {
            false
        } else this.userId == other.userId && this.permissionId == other.permissionId
    }

    override fun hashCode(): Int {
        return this.userId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(permissionId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserPermission> {
        override fun createFromParcel(parcel: Parcel): UserPermission {
            return UserPermission(parcel)
        }

        override fun newArray(size: Int): Array<UserPermission?> {
            return arrayOfNulls(size)
        }

        fun add(
            userId: Long,
            permissionId: Long,
        ): Boolean {
            if (userId < 1 || permissionId < 1) {
                return false
            }

            val i = UserPermissionDbHelper()
            return i.insert(userId, permissionId)
        }

        fun equals(a: Any?, b: Any): Boolean {
            return a != null && a == b
        }
    }
}