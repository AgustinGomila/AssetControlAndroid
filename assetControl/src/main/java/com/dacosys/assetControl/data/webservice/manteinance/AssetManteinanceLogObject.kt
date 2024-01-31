package com.dacosys.assetControl.data.webservice.manteinance

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.model.manteinance.AssetManteinance
import com.dacosys.assetControl.utils.Statics
import org.ksoap2.serialization.SoapObject

class AssetManteinanceLogObject() : Parcelable {
    /*
    <xsd:element name="asset_manteinance_id" type="xsd:int"/>
    <xsd:element name="manteinance_status_id" type="xsd:int"/>
    <xsd:element name="description" type="xsd:string"/>
    <xsd:element name="log_date" type="xsd:string"/>
    <xsd:element name="repairshop_id" type="xsd:int"/>
    <xsd:element name="repairman_id" type="xsd:int"/>
    */

    var asset_manteinance_id = 0L
    var manteinance_status_id = 0
    var description = ""
    var log_date = ""
    var asset_id = 0L
    var repairshop_id = 0L
    var repairman_id = 0L

    constructor(parcel: Parcel) : this() {
        asset_manteinance_id = parcel.readLong()
        manteinance_status_id = parcel.readInt()
        description = parcel.readString() ?: ""
        log_date = parcel.readString() ?: ""
        asset_id = parcel.readLong()
        repairshop_id = parcel.readLong()
        repairman_id = parcel.readLong()
    }

    constructor(assetManteinance: AssetManteinance) : this() {
        // Main Information
        asset_manteinance_id = assetManteinance.assetManteinanceId
        manteinance_status_id = assetManteinance.manteinanceStatusId
        description = assetManteinance.observations
        log_date = "" // VER ESTO!!!
        asset_id = assetManteinance.assetId
        repairshop_id = 0 // VER ESTO!!!
        repairman_id = Statics.currentUserId ?: 0
    }

    fun getBySoapObject(so: SoapObject): AssetManteinanceLogObject {
        val x = AssetManteinanceLogObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                /*
                <xsd:element name="asset_manteinance_id" type="xsd:int"/>
                <xsd:element name="manteinance_status_id" type="xsd:int"/>
                <xsd:element name="description" type="xsd:string"/>
                <xsd:element name="log_date" type="xsd:string"/>
                <xsd:element name="repairshop_id" type="xsd:int"/>
                <xsd:element name="repairman_id" type="xsd:int"/>
                */

                if (soValue != null)
                    when (soName) {
                        "asset_manteinance_id" -> {
                            x.asset_manteinance_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "manteinance_status_id" -> {
                            x.manteinance_status_id =
                                soValue as? Int ?: (soValue as? Long)?.toInt() ?: 0
                        }

                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }

                        "log_date" -> {
                            x.log_date = soValue as? String ?: ""
                        }

                        "asset_id" -> {
                            x.asset_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "repairshop_id" -> {
                            x.repairshop_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "repairman_id" -> {
                            x.repairman_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(asset_manteinance_id)
        parcel.writeInt(manteinance_status_id)
        parcel.writeString(description)
        parcel.writeString(log_date)
        parcel.writeLong(asset_id)
        parcel.writeLong(repairshop_id)
        parcel.writeLong(repairman_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetManteinanceLogObject> {
        override fun createFromParcel(parcel: Parcel): AssetManteinanceLogObject {
            return AssetManteinanceLogObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetManteinanceLogObject?> {
            return arrayOfNulls(size)
        }
    }
}


