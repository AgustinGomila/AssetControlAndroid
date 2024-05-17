package com.dacosys.assetControl.data.enums.attribute

import android.os.Parcel
import android.os.Parcelable

data class AttributeType(val id: Int, val description: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeType> {
        override fun createFromParcel(parcel: Parcel): AttributeType {
            return AttributeType(parcel)
        }

        override fun newArray(size: Int): Array<AttributeType?> {
            return arrayOfNulls(size)
        }

        var unknown = AttributeType(id = 0, description = "Desconocido")
        var system = AttributeType(id = 1, description = "Sistema")
        var userCanModify = AttributeType(id = 2, description = "Modificable por el usuario")
        var userDisabled = AttributeType(id = 3, description = "Desactivado por el usuario")

        fun getAll(): List<AttributeType> {
            return listOf(unknown, system, userCanModify, userDisabled)
        }

        fun getById(id: Int): AttributeType {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}

