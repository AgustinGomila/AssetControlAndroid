package com.dacosys.assetControl.data.model.attribute

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCategoryContract.AttributeCategoryEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCategoryContract.AttributeCategoryEntry.Companion.ATTRIBUTE_CATEGORY_ID
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCategoryContract.AttributeCategoryEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCategoryContract.AttributeCategoryEntry.Companion.PARENT_ID
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCategoryDbHelper

class AttributeCategory : Parcelable {
    var attributeCategoryId: Long = 0
    private var dataRead: Boolean = false

    val parent: AttributeCategory?
        get() =
            when {
                parentId == null -> null
                parentId!! > 0 -> AttributeCategory(parentId!!, false)
                else -> null
            }

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        attributeCategoryId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
    ) {
        this.attributeCategoryId = attributeCategoryId
        this.description = description
        this.active = active
        this.parentId = parentId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        attributeCategoryId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = AttributeCategoryDbHelper().selectById(this.attributeCategoryId)

        dataRead = true
        return when {
            temp != null -> {
                attributeCategoryId = temp.attributeCategoryId
                active = temp.active
                description = temp.description
                parentId = temp.parentId

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
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var active: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var parentId: Long? = 0
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    constructor(parcel: android.os.Parcel) {
        attributeCategoryId = parcel.readLong()
        parentId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ATTRIBUTE_CATEGORY_ID, attributeCategoryId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        values.put(PARENT_ID, parentId)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AttributeCategoryDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AttributeCategory) {
            false
        } else equals(this.attributeCategoryId, other.attributeCategoryId)
    }

    override fun hashCode(): Int {
        return this.attributeCategoryId.hashCode()
    }

    class CustomComparator : Comparator<AttributeCategory> {
        override fun compare(o1: AttributeCategory, o2: AttributeCategory): Int {
            if (o1.attributeCategoryId < o2.attributeCategoryId) {
                return -1
            } else if (o1.attributeCategoryId > o2.attributeCategoryId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(attributeCategoryId)
        parcel.writeLong(if (parentId == null) 0 else parentId ?: return)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeCategory> {
        override fun createFromParcel(parcel: android.os.Parcel): AttributeCategory {
            return AttributeCategory(parcel)
        }

        override fun newArray(size: Int): Array<AttributeCategory?> {
            return arrayOfNulls(size)
        }
    }
}