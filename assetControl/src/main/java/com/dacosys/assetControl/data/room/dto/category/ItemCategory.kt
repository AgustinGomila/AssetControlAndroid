package com.dacosys.assetControl.data.room.dto.category

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject

class ItemCategory(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var active: Int = 0,
    @ColumnInfo(name = Entry.PARENT_ID) var parentId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.PARENT_STR) var parentStr: String = ""
) : Parcelable {

    override fun toString(): String {
        return description
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        parentId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        parentStr = parcel.readString().orEmpty()
    )

    constructor(ic: ItemCategoryObject) : this(
        id = ic.item_category_id,
        description = ic.description,
        active = ic.active,
        parentId = ic.parent_id,
        transferred = 1
    )

    object Entry {
        const val TABLE_NAME = "item_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
        const val TRANSFERRED = "transferred"

        const val PARENT_STR = "parent_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(parentId)
        parcel.writeValue(transferred)
        parcel.writeString(parentStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemCategory> {
        override fun createFromParcel(parcel: Parcel): ItemCategory {
            return ItemCategory(parcel)
        }

        override fun newArray(size: Int): Array<ItemCategory?> {
            return arrayOfNulls(size)
        }
    }
}
