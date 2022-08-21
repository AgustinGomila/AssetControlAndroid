package com.dacosys.assetControl.model.assets.attributes.attribute.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeContract.AttributeEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeContract.AttributeEntry.Companion.ATTRIBUTE_CATEGORY_ID
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeContract.AttributeEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeContract.AttributeEntry.Companion.ATTRIBUTE_TYPE_ID
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeContract.AttributeEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.`object`.AttributeCategory
import com.dacosys.assetControl.model.assets.attributes.attributeType.AttributeType

class Attribute : Parcelable {
    var attributeId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        attributeId: Long,
        description: String,
        active: Boolean,
        attributeTypeId: Long,
        attributeCategoryId: Long,
    ) {
        this.attributeId = attributeId
        this.description = description
        this.active = active
        this.attributeTypeId = attributeTypeId
        this.attributeCategoryId = attributeCategoryId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        attributeId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = AttributeDbHelper().selectById(this.attributeId)

        dataRead = true
        return when {
            temp != null -> {
                attributeId = temp.attributeId
                active = temp.active
                description = temp.description
                attributeTypeId = temp.attributeTypeId
                attributeCategoryId = temp.attributeCategoryId

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

    val attributeType: AttributeType?
        get() =
            when {
                attributeTypeId!! > 0 -> AttributeType.getById(attributeTypeId!!)
                else -> null
            }

    private var attributeTypeId: Long? = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    val attributeCategory: AttributeCategory?
        get() =
            when {
                attributeCategoryId!! > 0 -> AttributeCategory(
                    attributeCategoryId!!,
                    false
                )
                else -> null
            }

    private var attributeCategoryId: Long? = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    constructor(parcel: android.os.Parcel) {
        attributeId = parcel.readLong()
        attributeTypeId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""
        attributeCategoryId = parcel.readLong()

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ATTRIBUTE_ID, attributeId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        values.put(ATTRIBUTE_TYPE_ID, attributeTypeId)
        values.put(ATTRIBUTE_CATEGORY_ID, attributeCategoryId)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AttributeDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Attribute) {
            false
        } else equals(this.attributeId, other.attributeId)
    }

    override fun hashCode(): Int {
        return this.attributeId.hashCode()
    }

    class CustomComparator : Comparator<Attribute> {
        override fun compare(o1: Attribute, o2: Attribute): Int {
            if (o1.attributeId < o2.attributeId) {
                return -1
            } else if (o1.attributeId > o2.attributeId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(attributeId)
        parcel.writeLong(if (attributeTypeId == null) 0L else attributeTypeId ?: return)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)
        parcel.writeLong(if (attributeCategoryId == null) 0L else attributeCategoryId ?: return)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Attribute> {
        override fun createFromParcel(parcel: android.os.Parcel): Attribute {
            return Attribute(parcel)
        }

        override fun newArray(size: Int): Array<Attribute?> {
            return arrayOfNulls(size)
        }

        fun add(
            attributeId: Long,
            description: String,
            active: Boolean,
            attributeTypeId: Long,
            attributeCategoryId: Long,
        ): Attribute? {
            if (description.isEmpty()) {
                return null
            }

            val i = AttributeDbHelper()
            val ok = i.insert(
                attributeId,
                description,
                active,
                attributeTypeId,
                attributeCategoryId
            )
            return if (ok) i.selectById(attributeId) else null
        }
    }
}