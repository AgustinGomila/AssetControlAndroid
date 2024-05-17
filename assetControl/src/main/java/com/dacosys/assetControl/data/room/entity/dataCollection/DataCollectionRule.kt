package com.dacosys.assetControl.data.room.entity.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRule.Entry
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionRuleObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class DataCollectionRule(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0
) : Parcelable {
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

    object Entry {
        const val TABLE_NAME = "data_collection_rule"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
    }

    override fun describeContents(): Int {
        return 0
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

