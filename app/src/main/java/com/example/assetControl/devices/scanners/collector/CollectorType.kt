package com.example.assetControl.devices.scanners.collector

import android.os.Parcel
import android.os.Parcelable

class CollectorType : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(collectorTypeId: Int, description: String) {
        this.description = description
        this.id = collectorTypeId
    }

    override fun toString() = description
    override fun equals(other: Any?) = other is CollectorType && id == other.id
    override fun hashCode() = id

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString().orEmpty()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeInt(id)
            writeString(description)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CollectorType> {
        override fun createFromParcel(parcel: Parcel): CollectorType = CollectorType(parcel)
        override fun newArray(size: Int): Array<CollectorType?> = arrayOfNulls(size)

        private val allTypes by lazy {
            listOf(
                CollectorType(0, "No configurado"),
                CollectorType(1, "Honeywell"),
                CollectorType(2, "Honeywell (nativo)"),
                CollectorType(3, "Zebra"),
            ).sortedBy { it.id }
        }

        val none get() = allTypes[0]
        val honeywell get() = allTypes[1]
        val honeywellNative get() = allTypes[2]
        val zebra get() = allTypes[3]

        fun getAll() = allTypes
        fun getById(id: Int) = allTypes.firstOrNull { it.id == id } ?: none
    }
}