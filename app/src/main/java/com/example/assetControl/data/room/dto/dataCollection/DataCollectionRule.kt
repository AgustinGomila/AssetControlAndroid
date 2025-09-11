package com.example.assetControl.data.room.dto.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.example.assetControl.data.webservice.dataCollection.DataCollectionRuleObject

abstract class DataCollectionRuleEntry {
    companion object {
        const val TABLE_NAME = "data_collection_rule"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }
}

class DataCollectionRule(
    @ColumnInfo(name = DataCollectionRuleEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = DataCollectionRuleEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = DataCollectionRuleEntry.ACTIVE) val active: Int = 0
) : Parcelable {

    override fun toString(): String {
        return description
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt()
    )

    constructor(dcrObject: DataCollectionRuleObject) : this(
        id = dcrObject.dataCollectionRuleId,
        description = dcrObject.description,
        active = dcrObject.active
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataCollectionRule

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRule> {
        override fun createFromParcel(parcel: Parcel): DataCollectionRule {
            return DataCollectionRule(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRule?> {
            return arrayOfNulls(size)
        }
    }
}

