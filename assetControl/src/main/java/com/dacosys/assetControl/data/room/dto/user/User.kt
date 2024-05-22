package com.dacosys.assetControl.data.room.dto.user

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.repository.user.UserPermissionRepository
import com.dacosys.assetControl.data.webservice.user.UserObject

class User(
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.NAME) val name: String = "",
    @ColumnInfo(name = Entry.EXTERNAL_ID) val externalId: String? = null,
    @ColumnInfo(name = Entry.EMAIL) val email: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.PASSWORD) val password: String? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return name
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        name = parcel.readString().orEmpty(),
        externalId = parcel.readString(),
        email = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        password = parcel.readString()
    )

    constructor(uObj: UserObject) : this(
        id = uObj.user_id,
        name = uObj.name,
        externalId = uObj.external_id,
        email = uObj.email,
        active = uObj.active,
        password = uObj.password
    )

    object Entry {
        const val TABLE_NAME = "user"
        const val ID = "_id"
        const val NAME = "name"
        const val EXTERNAL_ID = "external_id"
        const val EMAIL = "email"
        const val ACTIVE = "active"
        const val PASSWORD = "password"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(externalId)
        parcel.writeString(email)
        parcel.writeInt(active)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

        fun hasPermission(permission: PermissionEntry): Boolean {
            return if (isLogged()) {
                UserPermissionRepository().selectByUserIdUserPermissionId(
                    userId = getUserId() ?: 0,
                    permissionId = permission.id
                ) != null
            } else {
                false
            }
        }
    }
}
