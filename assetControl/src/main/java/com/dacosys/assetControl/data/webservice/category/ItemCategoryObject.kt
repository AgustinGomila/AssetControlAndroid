package com.dacosys.assetControl.data.webservice.category

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import org.ksoap2.serialization.SoapObject

class ItemCategoryObject() : Parcelable {
    var item_category_id = 0L
    var parent_id = 0L
    var active = 0
    var description = String()
    var item_category_ext_id = String()

    constructor(parcel: Parcel) : this() {
        item_category_id = parcel.readLong()
        parent_id = parcel.readLong()
        active = parcel.readInt()
        description = parcel.readString().orEmpty()
        item_category_ext_id = parcel.readString().orEmpty()
    }

    constructor(itemCategory: ItemCategory) : this() {
        // Main Information
        description = itemCategory.description
        item_category_id = itemCategory.id
        parent_id = itemCategory.parentId
        active = itemCategory.active
        item_category_ext_id = ""
    }

    fun getBySoapObject(so: SoapObject): ItemCategoryObject {
        val x = ItemCategoryObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "item_category_id" -> {
                            x.item_category_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "parent_id" -> {
                            x.parent_id =
                                (soValue as? Int)?.toLong() ?: if (soValue is Long) soValue else 0L
                        }

                        "item_category_ext_id" -> {
                            x.item_category_ext_id = soValue as? String ?: ""
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
        parcel.writeLong(item_category_id)
        parcel.writeLong(parent_id)
        parcel.writeInt(active)
        parcel.writeString(description)
        parcel.writeString(item_category_ext_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemCategoryObject> {
        override fun createFromParcel(parcel: Parcel): ItemCategoryObject {
            return ItemCategoryObject(parcel)
        }

        override fun newArray(size: Int): Array<ItemCategoryObject?> {
            return arrayOfNulls(size)
        }
    }
}


