package com.dacosys.assetControl.model.users.user.`object`

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.permissions.PermissionEntry
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.EMAIL
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.EXTERNAL_ID
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.PASSWORD
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.USER_ID
import com.dacosys.assetControl.model.users.user.dbHelper.UserDbHelper
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionDbHelper

class User : Parcelable {
    // setters
    var userId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        userId: Long,
        name: String,
        externalId: String?,
        email: String,
        active: Boolean,
        password: String,
    ) {
        this.userId = userId
        this.name = name
        this.externalId = externalId
        this.email = email
        this.active = active
        this.password = password

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        userId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = UserDbHelper().selectById(this.userId)
        dataRead = true

        return when {
            temp != null -> {
                userId = temp.userId
                active = temp.active
                name = temp.name
                externalId = temp.externalId
                email = temp.email
                password = temp.password

                true
            }
            else -> false
        }
    }

    override fun toString(): String {
        return name
    }

    var name: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var externalId: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var email: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var active: Boolean = false
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return false
                }
            }
            return field
        }

    var password: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    constructor(parcel: Parcel) {
        userId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        name = parcel.readString() ?: ""
        externalId = parcel.readString()
        email = parcel.readString() ?: ""
        password = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(USER_ID, userId)
        values.put(NAME, name)
        values.put(EXTERNAL_ID, externalId)
        values.put(EMAIL, email)
        values.put(ACTIVE, active)
        values.put(PASSWORD, password)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return UserDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is User) {
            false
        } else this.userId == other.userId
    }

    override fun hashCode(): Int {
        return this.userId.hashCode()
    }

    class CustomComparator : Comparator<User> {
        override fun compare(o1: User, o2: User): Int {
            if (o1.userId < o2.userId) {
                return -1
            } else if (o1.userId > o2.userId) {
                return 1
            }
            return 0
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(name)
        parcel.writeString(externalId)
        parcel.writeString(email)
        parcel.writeString(password)

        parcel.writeByte(if (dataRead) 1 else 0)
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

        fun add(
            userId: Long,
            name: String,
            externalId: String?,
            email: String,
            active: Boolean,
            password: String,
        ): User? {
            if (name.isEmpty() || password.isEmpty()) {
                return null
            }

            val i = UserDbHelper()
            val ok = i.insert(
                userId,
                name,
                externalId,
                email,
                active,
                password
            )
            return if (ok) i.selectById(userId) else null
        }

        fun equals(a: Any?, b: Any): Boolean {
            return a != null && a == b
        }

        fun hasPermission(permission: PermissionEntry): Boolean {
            return if (Statics.currentUserId != null) {
                val up = UserPermissionDbHelper().selectByUserIdUserPermissionId(
                    userId = Statics.currentUserId!!,
                    userPermissionId = permission.id
                )
                up != null
            } else {
                false
            }
        }
    }
}