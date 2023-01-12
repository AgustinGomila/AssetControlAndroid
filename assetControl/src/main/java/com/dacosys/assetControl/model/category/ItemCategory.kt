package com.dacosys.assetControl.model.category

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.PARENT_ID
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.dataBase.category.ItemCategoryDbHelper
import com.dacosys.assetControl.webservice.category.ItemCategoryObject

class ItemCategory : Parcelable {
    var itemCategoryId: Long = 0
    private var dataRead: Boolean = false

    val parent: ItemCategory?
        get() =
            when {
                parentId == null -> null
                parentId!! > 0 -> ItemCategory(parentId!!, false)
                else -> null
            }

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        itemCategoryId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
        transferred: Boolean,
    ) {
        this.itemCategoryId = itemCategoryId
        this.description = description
        this.active = active
        this.parentId = parentId
        this.transferred = transferred

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        itemCategoryId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = ItemCategoryDbHelper().selectById(this.itemCategoryId)

        dataRead = true
        return when {
            temp != null -> {
                itemCategoryId = temp.itemCategoryId
                active = temp.active
                description = temp.description
                parentId = temp.parentId
                transferred = temp.transferred

                true
            }
            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    var description: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var active: Boolean = false
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return false
                }
            }
            return field
        }

    var parentId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var parentStr: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var transferred: Boolean? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    constructor()

    constructor(parcel: android.os.Parcel) {
        itemCategoryId = parcel.readLong()
        parentId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""
        transferred = parcel.readByte() != 0.toByte()
        parentStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    constructor(ic: ItemCategoryObject) {
        itemCategoryId = ic.item_category_id
        parentId = ic.parent_id
        active = ic.active == 1
        description = ic.description
        transferred = false
        parentStr = ""

        dataRead = true
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, itemCategoryId)
        values.put(PARENT_ID, parentId)
        values.put(ACTIVE, active)
        values.put(DESCRIPTION, description)
        values.put(TRANSFERRED, transferred)

        //values.put(PARENT_STR, parentStr)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return ItemCategoryDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ItemCategory) {
            false
        } else equals(this.itemCategoryId, other.itemCategoryId)
    }

    override fun hashCode(): Int {
        return this.itemCategoryId.hashCode()
    }

    class CustomComparator : Comparator<ItemCategory> {
        override fun compare(o1: ItemCategory, o2: ItemCategory): Int {
            if (o1.itemCategoryId < o2.itemCategoryId) {
                return -1
            } else if (o1.itemCategoryId > o2.itemCategoryId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(itemCategoryId)
        parcel.writeLong(if (parentId == null) 0 else parentId ?: return)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)
        parcel.writeByte(if (transferred == true) 1 else 0)
        parcel.writeString(parentStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemCategory> {
        override fun createFromParcel(parcel: android.os.Parcel): ItemCategory {
            return ItemCategory(parcel)
        }

        override fun newArray(size: Int): Array<ItemCategory?> {
            return arrayOfNulls(size)
        }

        fun add(
            itemCategoryId: Long,
            description: String,
            active: Boolean,
            parentId: Long,
            transferred: Boolean,
        ): ItemCategory? {
            if (description.isEmpty()) {
                return null
            }

            val i = ItemCategoryDbHelper()
            val ok = i.insert(
                itemCategoryId,
                description,
                active,
                parentId,
                transferred
            )
            return if (ok) i.selectById(itemCategoryId) else null
        }
    }
}