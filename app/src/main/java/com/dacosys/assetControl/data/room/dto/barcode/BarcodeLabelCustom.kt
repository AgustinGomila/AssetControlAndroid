package com.dacosys.assetControl.data.room.dto.barcode

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.barcode.BarcodeLabelCustomObject

abstract class BarcodeLabelCustomEntry {
    companion object {
        const val TABLE_NAME = "barcode_label_custom"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val BARCODE_LABEL_TARGET_ID = "barcode_label_target_id"
        const val TEMPLATE = "template"
    }
}

class BarcodeLabelCustom(
    @ColumnInfo(name = BarcodeLabelCustomEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = BarcodeLabelCustomEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = BarcodeLabelCustomEntry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID) val barcodeLabelTargetId: Long = 0L,
    @ColumnInfo(name = BarcodeLabelCustomEntry.TEMPLATE) val template: String = ""
) : Parcelable {

    override fun toString(): String {
        return description
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        barcodeLabelTargetId = parcel.readLong(),
        template = parcel.readString().orEmpty()
    )

    constructor(blcObject: BarcodeLabelCustomObject) : this(
        id = blcObject.barcode_label_custom_id,
        description = blcObject.description,
        active = blcObject.active,
        barcodeLabelTargetId = blcObject.barcode_label_target_id,
        template = blcObject.template
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(barcodeLabelTargetId)
        parcel.writeString(template)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarcodeLabelCustom

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<BarcodeLabelCustom> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelCustom {
            return BarcodeLabelCustom(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelCustom?> {
            return arrayOfNulls(size)
        }
    }
}