package com.dacosys.assetControl.data.model.attribute

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class AttributeType : Parcelable {
    var id: Long = 0
    var description: String = ""

    constructor(attributeTypeId: Long, description: String) {
        this.description = description
        this.id = attributeTypeId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AttributeType) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
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

        var unknown = AttributeType(
            attributeTypeId = 0,
            description = "Desconocido"
        )
        var system = AttributeType(
            attributeTypeId = 1,
            description = "Sistema"
        )
        private var userCanModify = AttributeType(
            attributeTypeId = 2,
            description = "Modificable por el usuario"
        )
        private var userDisabled = AttributeType(
            attributeTypeId = 3,
            description = "Desactivado por el usuario"
        )

        fun getAll(): ArrayList<AttributeType> {
            val allSections = ArrayList<AttributeType>()
            Collections.addAll(
                allSections,
                unknown,
                system,
                userCanModify,
                userDisabled
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(attributeTypeId: Long): AttributeType? {
            return getAll().firstOrNull { it.id == attributeTypeId }
        }
    }
}