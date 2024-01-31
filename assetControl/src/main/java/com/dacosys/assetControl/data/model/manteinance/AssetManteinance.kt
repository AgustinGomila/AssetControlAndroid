package com.dacosys.assetControl.data.model.manteinance

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.ASSET_MANTEINANCE_ID
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.COLLECTOR_ASSET_MANTEINANCE_ID
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.MANTEINANCE_STATUS_ID
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.MANTEINANCE_TYPE_ID
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.OBSERVATIONS
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.data.dataBase.manteinance.AssetManteinanceDbHelper
import com.dacosys.assetControl.data.model.asset.Asset

class AssetManteinance : Parcelable {
    var collectorAssetManteinanceId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        assetManteinanceId: Long,
        manteinanceTypeId: Long,
        manteinanceStatusId: Int,
        assetId: Long,
        observtions: String,
        transferred: Boolean,
        collectorAssetManteinanceId: Long,
    ) {
        this.collectorAssetManteinanceId = collectorAssetManteinanceId
        this.assetManteinanceId = assetManteinanceId
        this.manteinanceTypeId = manteinanceTypeId
        this.manteinanceStatusId = manteinanceStatusId
        this.assetId = assetId
        this.observations = observtions
        this.transferred = transferred

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorAssetManteinanceId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = AssetManteinanceDbHelper().selectById(this.collectorAssetManteinanceId)

        dataRead = true
        return when {
            temp != null -> {
                collectorAssetManteinanceId = temp.collectorAssetManteinanceId
                assetManteinanceId = temp.assetManteinanceId
                manteinanceTypeId = temp.manteinanceTypeId
                manteinanceStatusId = temp.manteinanceStatusId
                assetId = temp.assetId
                observations = temp.observations
                transferred = temp.transferred

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return ""
    }

    val asset: Asset
        get() {
            return Asset(assetId, false)
        }

    var assetId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val assetStr: String
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return asset.description
        }

    val assetCode: String
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return asset.code
        }

    val manteinanceType: ManteinanceType
        get() {
            return ManteinanceType(manteinanceTypeId, false)
        }

    var manteinanceTypeId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    val manteinanceTypeStr: String
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return manteinanceType.description
        }

    val manteinanceStatus: ManteinanceStatus?
        get() {
            return ManteinanceStatus.getById(manteinanceStatusId)
        }

    var manteinanceStatusId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var assetManteinanceId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var observations: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var transferred: Boolean? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    constructor(parcel: android.os.Parcel) {
        collectorAssetManteinanceId = parcel.readLong()
        assetManteinanceId = parcel.readLong()
        manteinanceTypeId = parcel.readLong()
        manteinanceStatusId = parcel.readInt()
        assetId = parcel.readLong()
        observations = parcel.readString() ?: ""
        transferred = parcel.readByte() != 0.toByte()

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(COLLECTOR_ASSET_MANTEINANCE_ID, collectorAssetManteinanceId)
        values.put(ASSET_MANTEINANCE_ID, assetManteinanceId)
        values.put(MANTEINANCE_TYPE_ID, manteinanceTypeId)
        values.put(MANTEINANCE_STATUS_ID, manteinanceStatusId)
        values.put(ASSET_ID, assetId)
        values.put(OBSERVATIONS, observations)
        values.put(TRANSFERRED, transferred)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return AssetManteinanceDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AssetManteinance) {
            false
        } else equals(this.collectorAssetManteinanceId, other.collectorAssetManteinanceId)
    }

    override fun hashCode(): Int {
        return this.collectorAssetManteinanceId.hashCode()
    }

    class CustomComparator : Comparator<AssetManteinance> {
        override fun compare(o1: AssetManteinance, o2: AssetManteinance): Int {
            if (o1.collectorAssetManteinanceId < o2.collectorAssetManteinanceId) {
                return -1
            } else if (o1.collectorAssetManteinanceId > o2.collectorAssetManteinanceId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(collectorAssetManteinanceId)
        parcel.writeLong(assetManteinanceId)
        parcel.writeLong(manteinanceTypeId)
        parcel.writeInt(manteinanceStatusId)
        parcel.writeLong(assetId)
        parcel.writeString(observations)
        parcel.writeInt(if (transferred == true) 1 else 0)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetManteinance> {
        override fun createFromParcel(parcel: android.os.Parcel): AssetManteinance {
            return AssetManteinance(parcel)
        }

        override fun newArray(size: Int): Array<AssetManteinance?> {
            return arrayOfNulls(size)
        }
    }
}