package com.dacosys.assetControl.data.model.barcode

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.BARCODE_LABEL_CUSTOM_ID
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.BARCODE_LABEL_TARGET_ID
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.TEMPLATE
import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelCustomDbHelper

class BarcodeLabelCustom : Parcelable {
    var barcodeLabelCustomId: Long = 0
    private var dataRead: Boolean = false

    val barcodeLabelTarget: BarcodeLabelTarget?
        get() =
            when {
                barcodeLabelTargetId == null -> null
                barcodeLabelTargetId!! > 0 -> BarcodeLabelTarget.getById(barcodeLabelTargetId!!)
                else -> null
            }

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        barcodeLabelCustomId: Long,
        description: String,
        active: Boolean,
        barcodeLabelTargetId: Long,
        template: String,
    ) {
        this.barcodeLabelCustomId = barcodeLabelCustomId
        this.description = description
        this.active = active
        this.barcodeLabelTargetId = barcodeLabelTargetId
        this.template = template

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        barcodeLabelCustomId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = BarcodeLabelCustomDbHelper().selectById(this.barcodeLabelCustomId)

        dataRead = true
        return when {
            temp != null -> {
                barcodeLabelCustomId = temp.barcodeLabelCustomId
                active = temp.active
                description = temp.description
                barcodeLabelTargetId = temp.barcodeLabelTargetId
                template = temp.template

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

    var barcodeLabelTargetId: Long? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var template: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    constructor(parcel: android.os.Parcel) {
        barcodeLabelCustomId = parcel.readLong()
        barcodeLabelTargetId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""
        template = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(BARCODE_LABEL_CUSTOM_ID, barcodeLabelCustomId)
        values.put(BARCODE_LABEL_TARGET_ID, barcodeLabelTargetId)
        values.put(ACTIVE, active)
        values.put(DESCRIPTION, description)
        values.put(TEMPLATE, template)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return BarcodeLabelCustomDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is BarcodeLabelCustom) {
            false
        } else equals(this.barcodeLabelCustomId, other.barcodeLabelCustomId)
    }

    override fun hashCode(): Int {
        return this.barcodeLabelCustomId.hashCode()
    }

    class CustomComparator : Comparator<BarcodeLabelCustom> {
        override fun compare(o1: BarcodeLabelCustom, o2: BarcodeLabelCustom): Int {
            if (o1.barcodeLabelCustomId < o2.barcodeLabelCustomId) {
                return -1
            } else if (o1.barcodeLabelCustomId > o2.barcodeLabelCustomId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(barcodeLabelCustomId)
        parcel.writeLong(barcodeLabelTargetId ?: 0L)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)
        parcel.writeString(template)
        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BarcodeLabelCustom> {
        override fun createFromParcel(parcel: android.os.Parcel): BarcodeLabelCustom {
            return BarcodeLabelCustom(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelCustom?> {
            return arrayOfNulls(size)
        }
    }
}