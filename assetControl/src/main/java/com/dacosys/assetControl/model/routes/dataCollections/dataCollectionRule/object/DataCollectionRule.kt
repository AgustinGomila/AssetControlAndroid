package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleDbHelper

class DataCollectionRule : Parcelable {
    var dataCollectionRuleId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        dataCollectionRuleId: Long,
        description: String,
        active: Boolean,
    ) {
        this.dataCollectionRuleId = dataCollectionRuleId
        this.description = description
        this.active = active

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        dataCollectionRuleId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = DataCollectionRuleDbHelper().selectById(this.dataCollectionRuleId)

        dataRead = true
        return when {
            temp != null -> {
                dataCollectionRuleId = temp.dataCollectionRuleId
                active = temp.active
                description = temp.description

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

    constructor(parcel: android.os.Parcel) {
        dataCollectionRuleId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(DATA_COLLECTION_RULE_ID, dataCollectionRuleId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return DataCollectionRuleDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DataCollectionRule) {
            false
        } else equals(this.dataCollectionRuleId, other.dataCollectionRuleId)
    }

    override fun hashCode(): Int {
        return this.dataCollectionRuleId.hashCode()
    }

    class CustomComparator : Comparator<DataCollectionRule> {
        override fun compare(o1: DataCollectionRule, o2: DataCollectionRule): Int {
            if (o1.dataCollectionRuleId < o2.dataCollectionRuleId) {
                return -1
            } else if (o1.dataCollectionRuleId > o2.dataCollectionRuleId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRule> {
        override fun createFromParcel(parcel: android.os.Parcel): DataCollectionRule {
            return DataCollectionRule(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRule?> {
            return arrayOfNulls(size)
        }

        fun add(
            dataCollectionRuleId: Long,
            description: String,
            active: Boolean,
        ): DataCollectionRule? {
            if (description.isEmpty()) {
                return null
            }

            val i = DataCollectionRuleDbHelper()
            val ok = i.insert(
                dataCollectionRuleId,
                description,
                active
            )
            return if (ok) i.selectById(dataCollectionRuleId) else null
        }
    }
}