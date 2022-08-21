package com.dacosys.assetControl.model.assets.manteinances.manteinanceType.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.MANTEINANCE_TYPE_GROUP_ID
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.MANTEINANCE_TYPE_ID
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.`object`.ManteinanceTypeGroup

class ManteinanceType : Parcelable {
    var manteinanceTypeId: Long = 0
    private var dataRead: Boolean = false

    val manteinanceTypeGroup: ManteinanceTypeGroup?
        get() =
            when {
                manteinanceTypeGroupId != 0L -> ManteinanceTypeGroup(
                    manteinanceTypeGroupId,
                    false
                )
                else -> null
            }

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        manteinanceTypeId: Long,
        description: String,
        active: Boolean,
        manteinanceTypeGroupId: Long,
    ) {
        this.manteinanceTypeId = manteinanceTypeId
        this.description = description
        this.active = active
        this.manteinanceTypeGroupId = manteinanceTypeGroupId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        manteinanceTypeId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = ManteinanceTypeDbHelper().selectById(this.manteinanceTypeId)

        dataRead = true
        return when {
            temp != null -> {
                manteinanceTypeId = temp.manteinanceTypeId
                active = temp.active
                description = temp.description
                manteinanceTypeGroupId = temp.manteinanceTypeGroupId

                true
            }
            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    var description: String = ""
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

    var manteinanceTypeGroupId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    constructor(parcel: android.os.Parcel) {
        manteinanceTypeId = parcel.readLong()
        manteinanceTypeGroupId = parcel.readLong()
        description = parcel.readString() ?: ""
        active = parcel.readByte() != 0.toByte()

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(MANTEINANCE_TYPE_ID, manteinanceTypeId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        values.put(MANTEINANCE_TYPE_GROUP_ID, manteinanceTypeGroupId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return ManteinanceTypeDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ManteinanceType) {
            false
        } else equals(this.manteinanceTypeId, other.manteinanceTypeId)
    }

    override fun hashCode(): Int {
        return this.manteinanceTypeId.hashCode()
    }

    class CustomComparator : Comparator<ManteinanceType> {
        override fun compare(o1: ManteinanceType, o2: ManteinanceType): Int {
            if (o1.manteinanceTypeId < o2.manteinanceTypeId) {
                return -1
            } else if (o1.manteinanceTypeId > o2.manteinanceTypeId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(manteinanceTypeId)
        parcel.writeLong(manteinanceTypeGroupId)
        parcel.writeString(description)
        parcel.writeByte(if (active) 1 else 0)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ManteinanceType> {
        override fun createFromParcel(parcel: android.os.Parcel): ManteinanceType {
            return ManteinanceType(parcel)
        }

        override fun newArray(size: Int): Array<ManteinanceType?> {
            return arrayOfNulls(size)
        }

        fun add(
            manteinanceTypeId: Long,
            description: String,
            active: Boolean,
            manteinanceTypeGroupId: Long,
        ): ManteinanceType? {
            if (description.isEmpty()) {
                return null
            }

            val i = ManteinanceTypeDbHelper()
            val ok = i.insert(
                manteinanceTypeId,
                description,
                active,
                manteinanceTypeGroupId
            )
            return if (ok) i.selectById(manteinanceTypeId) else null
        }
    }
}