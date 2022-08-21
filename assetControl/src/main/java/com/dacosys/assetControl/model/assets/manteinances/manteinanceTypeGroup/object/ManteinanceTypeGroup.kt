package com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupContract.ManteinanceTypeGroupEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupContract.ManteinanceTypeGroupEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupContract.ManteinanceTypeGroupEntry.Companion.MANTEINANCE_TYPE_GROUP_ID
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupDbHelper

class ManteinanceTypeGroup : Parcelable {
    var manteinanceTypeGroupId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        manteinanceTypeGroupId: Long,
        description: String,
        active: Boolean,
    ) {
        this.manteinanceTypeGroupId = manteinanceTypeGroupId
        this.description = description
        this.active = active

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        manteinanceTypeGroupId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = ManteinanceTypeGroupDbHelper().selectById(this.manteinanceTypeGroupId)

        dataRead = true
        return when {
            temp != null -> {
                manteinanceTypeGroupId = temp.manteinanceTypeGroupId
                active = temp.active
                description = temp.description

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

    constructor(parcel: android.os.Parcel) {
        manteinanceTypeGroupId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(MANTEINANCE_TYPE_GROUP_ID, manteinanceTypeGroupId)
        values.put(ACTIVE, active)
        values.put(DESCRIPTION, description)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return ManteinanceTypeGroupDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ManteinanceTypeGroup) {
            false
        } else equals(this.manteinanceTypeGroupId, other.manteinanceTypeGroupId)
    }

    override fun hashCode(): Int {
        return this.manteinanceTypeGroupId.hashCode()
    }

    class CustomComparator : Comparator<ManteinanceTypeGroup> {
        override fun compare(o1: ManteinanceTypeGroup, o2: ManteinanceTypeGroup): Int {
            if (o1.manteinanceTypeGroupId < o2.manteinanceTypeGroupId) {
                return -1
            } else if (o1.manteinanceTypeGroupId > o2.manteinanceTypeGroupId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(manteinanceTypeGroupId)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ManteinanceTypeGroup> {
        override fun createFromParcel(parcel: android.os.Parcel): ManteinanceTypeGroup {
            return ManteinanceTypeGroup(parcel)
        }

        override fun newArray(size: Int): Array<ManteinanceTypeGroup?> {
            return arrayOfNulls(size)
        }

        fun add(
            manteinanceTypeGroupId: Long,
            description: String,
            active: Boolean,
        ): ManteinanceTypeGroup? {
            if (description.isEmpty()) {
                return null
            }

            val i = ManteinanceTypeGroupDbHelper()
            val ok = i.insert(manteinanceTypeGroupId, description, active)
            return if (ok) i.selectById(manteinanceTypeGroupId) else null
        }
    }
}