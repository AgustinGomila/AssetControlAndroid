package com.dacosys.assetControl.data.room.entity.barcode

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom.Entry
import com.dacosys.assetControl.data.webservice.barcode.BarcodeLabelCustomObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.BARCODE_LABEL_TARGET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.BARCODE_LABEL_TARGET_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelCustom(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.BARCODE_LABEL_TARGET_ID) val barcodeLabelTargetId: Long = 0L,
    @ColumnInfo(name = Entry.TEMPLATE) val template: String = ""
) : Parcelable {
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

    object Entry {
        const val TABLE_NAME = "barcode_label_custom"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val BARCODE_LABEL_TARGET_ID = "barcode_label_target_id"
        const val TEMPLATE = "template"
    }

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

    companion object CREATOR : Parcelable.Creator<BarcodeLabelCustom> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelCustom {
            return BarcodeLabelCustom(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelCustom?> {
            return arrayOfNulls(size)
        }
    }
}