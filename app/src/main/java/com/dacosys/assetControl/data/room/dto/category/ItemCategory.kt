package com.dacosys.assetControl.data.room.dto.category

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject

abstract class ItemCategoryEntry {
    companion object {
        const val TABLE_NAME = "item_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
        const val TRANSFERRED = "transferred"
        const val PARENT_STR = "parent_str"
    }
}

class ItemCategory(
    @ColumnInfo(name = ItemCategoryEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = ItemCategoryEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = ItemCategoryEntry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = ItemCategoryEntry.PARENT_ID) var parentId: Long = 0L,
    @ColumnInfo(name = ItemCategoryEntry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = ItemCategoryEntry.PARENT_STR) var parentDescription: String? = null
) : Parcelable {

    override fun toString(): String {
        return description
    }

    @Ignore
    var active: Boolean = mActive == 1
        get() = mActive == 1
        set(value) {
            mActive = if (value) 1 else 0
            field = value
        }

    @Ignore
    var parentStr = parentDescription.orEmpty()
        get() = parentDescription.orEmpty()
        set(value) {
            parentDescription = value.ifEmpty { null }
            field = value
        }


    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        mActive = parcel.readInt(),
        parentId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        parentDescription = parcel.readString().orEmpty()
    )

    constructor(ic: ItemCategoryObject) : this(
        id = ic.item_category_id,
        description = ic.description,
        mActive = ic.active,
        parentId = ic.parent_id,
        transferred = 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(mActive)
        parcel.writeLong(parentId)
        parcel.writeValue(transferred)
        parcel.writeString(parentDescription)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemCategory

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
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
