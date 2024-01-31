package com.dacosys.assetControl.data.model.dataCollection

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContentDbHelper
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.COLLECTOR_DATA_COLLECTION_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.COLLECTOR_ROUTE_PROCESS_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.COMPLETED
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.DATE_END
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.DATE_START
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.USER_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionDbHelper

class DataCollection : Parcelable {
    var collectorDataCollectionId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        dataCollectionId: Long,
        assetId: Long,
        warehouseId: Long,
        warehouseAreaId: Long,
        userId: Long,
        dateStart: String,
        dateEnd: String,
        completed: Boolean,
        transferedDate: String?,
        collectorDataCollectionId: Long,
        collectorRouteProcessId: Long,
    ) {
        this.dataCollectionId = dataCollectionId
        this.assetId = assetId
        this.warehouseId = warehouseId
        this.warehouseAreaId = warehouseAreaId
        this.userId = userId
        this.dateStart = dateStart
        this.dateEnd = dateEnd
        this.completed = completed
        this.transferedDate = transferedDate
        this.collectorDataCollectionId = collectorDataCollectionId
        this.collectorRouteProcessId = collectorRouteProcessId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorDataCollectionId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp =
            DataCollectionDbHelper()
                .selectByCollectorId(this.collectorDataCollectionId)

        dataRead = true
        return when {
            temp != null -> {
                this.dataCollectionId = temp.dataCollectionId
                this.assetId = temp.assetId
                this.warehouseId = temp.warehouseId
                this.warehouseAreaId = temp.warehouseAreaId
                this.userId = temp.userId
                this.dateStart = temp.dateStart
                this.dateEnd = temp.dateEnd
                this.completed = temp.completed
                this.transferedDate = temp.transferedDate
                this.collectorDataCollectionId = temp.collectorDataCollectionId
                this.collectorRouteProcessId = temp.collectorRouteProcessId

                this.assetStr = temp.assetStr
                this.assetCode = temp.assetCode
                this.warehouseStr = temp.warehouseStr
                this.warehouseAreaStr = temp.warehouseAreaStr

                true
            }

            else -> false
        }
    }

    val contents: ArrayList<DataCollectionContent>
        get() {
            return DataCollectionContentDbHelper().selectByCollectorDataCollectionId(
                this.collectorDataCollectionId
            )
        }

    var completed: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    var collectorRouteProcessId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var dateStart: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var dateEnd: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var transferedDate: String? = null
        get() {
            return if (!dataRead && !refreshData()) null
            else field
        }

    var dataCollectionId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var userId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var assetId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var warehouseAreaId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var warehouseId: Long = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    var assetStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var assetCode: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var warehouseStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var warehouseAreaStr: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var statusId: Int = 0
        get() {
            return if (!dataRead && !refreshData()) 0
            else field
        }

    constructor(parcel: android.os.Parcel) {
        dataCollectionId = parcel.readLong()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        userId = parcel.readLong()
        dateStart = parcel.readString() ?: ""
        dateEnd = parcel.readString() ?: ""
        completed = parcel.readByte() != 0.toByte()
        transferedDate = parcel.readString()
        collectorDataCollectionId = parcel.readLong()
        collectorRouteProcessId = parcel.readLong()

        assetStr = parcel.readString() ?: ""
        assetCode = parcel.readString() ?: ""
        warehouseStr = parcel.readString() ?: ""
        warehouseAreaStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(DATA_COLLECTION_ID, dataCollectionId)
        values.put(ASSET_ID, assetId)
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(USER_ID, userId)
        values.put(DATE_START, dateStart)
        values.put(DATE_END, dateEnd)
        values.put(COMPLETED, completed)
        values.put(TRANSFERED_DATE, transferedDate)
        values.put(COLLECTOR_DATA_COLLECTION_ID, collectorDataCollectionId)
        values.put(COLLECTOR_ROUTE_PROCESS_ID, collectorRouteProcessId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return DataCollectionDbHelper()
            .update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DataCollection) {
            false
        } else equals(this.collectorDataCollectionId, other.collectorDataCollectionId)
    }

    override fun hashCode(): Int {
        return this.collectorDataCollectionId.hashCode()
    }

    class CustomComparator : Comparator<DataCollection> {
        override fun compare(o1: DataCollection, o2: DataCollection): Int {
            if (o1.collectorDataCollectionId < o2.collectorDataCollectionId) {
                return -1
            } else if (o1.collectorDataCollectionId > o2.collectorDataCollectionId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(dataCollectionId)
        parcel.writeLong(assetId)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(userId)
        parcel.writeString(dateStart)
        parcel.writeString(dateEnd)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeString(transferedDate)
        parcel.writeLong(collectorDataCollectionId)
        parcel.writeLong(collectorRouteProcessId)

        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeString(warehouseStr)
        parcel.writeString(warehouseAreaStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollection> {
        override fun createFromParcel(parcel: android.os.Parcel): DataCollection {
            return DataCollection(parcel)
        }

        override fun newArray(size: Int): Array<DataCollection?> {
            return arrayOfNulls(size)
        }
    }
}