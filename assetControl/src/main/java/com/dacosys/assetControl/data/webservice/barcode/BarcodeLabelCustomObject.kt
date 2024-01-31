package com.dacosys.assetControl.data.webservice.barcode

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.model.barcode.BarcodeLabelCustom
import org.ksoap2.serialization.SoapObject

class BarcodeLabelCustomObject() : Parcelable {
    var barcode_label_custom_id = 0L
    var barcode_label_target_id = 0L
    var active = 0
    var description = String()
    var template = String()

    constructor(parcel: Parcel) : this() {
        barcode_label_custom_id = parcel.readLong()
        barcode_label_target_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString() ?: ""
        template = parcel.readString() ?: ""
    }

    constructor(barcodeLabelCustom: BarcodeLabelCustom) : this() {
        // Main Information
        description = barcodeLabelCustom.description
        barcode_label_custom_id = barcodeLabelCustom.barcodeLabelCustomId
        barcode_label_target_id = barcodeLabelCustom.barcodeLabelTargetId ?: 0
        active = if (barcodeLabelCustom.active) 1 else 0
        template = barcodeLabelCustom.template
    }

    fun getBySoapObject(so: SoapObject): BarcodeLabelCustomObject {
        val x = BarcodeLabelCustomObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "barcode_label_custom_id" -> {
                            x.barcode_label_custom_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "barcode_label_target_id" -> {
                            x.barcode_label_target_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "template" -> {
                            x.template = soValue as? String ?: ""
                        }

                        "active" -> {
                            x.active = soValue as? Int ?: 0
                        }

                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(barcode_label_custom_id)
        parcel.writeLong(barcode_label_target_id)
        parcel.writeInt(active)
        parcel.writeString(description)
        parcel.writeString(template)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BarcodeLabelCustomObject> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelCustomObject {
            return BarcodeLabelCustomObject(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelCustomObject?> {
            return arrayOfNulls(size)
        }
    }
}


