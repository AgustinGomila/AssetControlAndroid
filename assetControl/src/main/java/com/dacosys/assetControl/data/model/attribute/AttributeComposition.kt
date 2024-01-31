package com.dacosys.assetControl.data.model.attribute

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_COMPOSITION_TYPE_ID
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.COMPOSITION
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.DEFAULT_VALUE
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.NAME
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.READ_ONLY
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry.Companion.USED
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionDbHelper

class AttributeComposition : Parcelable {
    var attributeCompositionId: Long = 0
    private var dataRead: Boolean = false

    constructor(
        attributeCompositionId: Long,
        attributeId: Long,
        attributeCompositionTypeId: Long,
        description: String,
        composition: String?,
        used: Boolean,
        name: String,
        readOnly: Boolean,
        defaultValue: String,
    ) {
        this.attributeCompositionId = attributeCompositionId
        this.attributeId = attributeId
        this.attributeCompositionTypeId = attributeCompositionTypeId
        this.description = description
        this.composition = composition
        this.used = used
        this.name = name
        this.readOnly = readOnly
        this.defaultValue = defaultValue

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        attributeCompositionId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = AttributeCompositionDbHelper().selectById(this.attributeCompositionId)

        dataRead = true
        return when {
            temp != null -> {
                attributeCompositionId = temp.attributeCompositionId
                attributeId = temp.attributeId
                attributeCompositionTypeId = temp.attributeCompositionTypeId
                description = temp.description
                composition = temp.composition
                used = temp.used
                name = temp.name
                readOnly = temp.readOnly
                defaultValue = temp.defaultValue

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    val attribute: Attribute?
        get() {
            return if (attributeId == 0L) null
            else Attribute(attributeId, false)
        }

    var attributeId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val attributeCompositionType: AttributeCompositionType?
        get() {
            return AttributeCompositionType.getById(attributeCompositionTypeId)
        }

    var attributeCompositionTypeId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var description: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var composition: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var name: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var defaultValue: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var used: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var readOnly: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    constructor(parcel: android.os.Parcel) {
        attributeCompositionId = parcel.readLong()
        attributeId = parcel.readLong()
        attributeCompositionTypeId = parcel.readLong()
        description = parcel.readString() ?: ""
        composition = parcel.readString()
        used = parcel.readByte() != 0.toByte()
        name = parcel.readString() ?: ""
        readOnly = parcel.readByte() != 0.toByte()
        defaultValue = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(ATTRIBUTE_COMPOSITION_ID, attributeCompositionId)
        values.put(DESCRIPTION, description)
        values.put(ATTRIBUTE_ID, attributeId)
        values.put(ATTRIBUTE_COMPOSITION_TYPE_ID, attributeCompositionTypeId)
        values.put(COMPOSITION, composition)
        values.put(USED, used)
        values.put(NAME, name)
        values.put(READ_ONLY, readOnly)
        values.put(DEFAULT_VALUE, defaultValue)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AttributeCompositionDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AttributeComposition) {
            false
        } else equals(this.attributeCompositionId, other.attributeCompositionId)
    }

    override fun hashCode(): Int {
        return this.attributeCompositionId.hashCode()
    }

    class CustomComparator : Comparator<AttributeComposition> {
        override fun compare(o1: AttributeComposition, o2: AttributeComposition): Int {
            if (o1.attributeCompositionId < o2.attributeCompositionId) {
                return -1
            } else if (o1.attributeCompositionId > o2.attributeCompositionId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(attributeCompositionId)
        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeComposition> {
        override fun createFromParcel(parcel: android.os.Parcel): AttributeComposition {
            return AttributeComposition(parcel)
        }

        override fun newArray(size: Int): Array<AttributeComposition?> {
            return arrayOfNulls(size)
        }
    }
}