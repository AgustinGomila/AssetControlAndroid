package com.dacosys.assetControl.data.room.dto.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionContentRepository
import java.util.*

abstract class DataCollectionEntry {
    companion object {
        const val TABLE_NAME = "data_collection"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val ASSET_ID = "asset_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val USER_ID = "user_id"
        const val DATE_START = "date_start"
        const val DATE_END = "date_end"
        const val COMPLETED = "completed"
        const val TRANSFERRED_DATE = "transferred_date"
        const val ID = "_id"
        const val ROUTE_PROCESS_ID = "route_process_id"

        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
    }
}

class DataCollection(
    @ColumnInfo(name = DataCollectionEntry.DATA_COLLECTION_ID) var dataCollectionId: Long = 0L,
    @ColumnInfo(name = DataCollectionEntry.ASSET_ID) var assetId: Long? = null,
    @ColumnInfo(name = DataCollectionEntry.WAREHOUSE_ID) var warehouseId: Long? = null,
    @ColumnInfo(name = DataCollectionEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long? = null,
    @ColumnInfo(name = DataCollectionEntry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = DataCollectionEntry.DATE_START) var dateStart: Date? = null,
    @ColumnInfo(name = DataCollectionEntry.DATE_END) var dateEnd: Date? = null,
    @ColumnInfo(name = DataCollectionEntry.COMPLETED) var completed: Int = 0,
    @ColumnInfo(name = DataCollectionEntry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = DataCollectionEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = DataCollectionEntry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = DataCollectionEntry.ASSET_STR) var assetDescription: String? = null,
    @ColumnInfo(name = DataCollectionEntry.ASSET_CODE) var assetCode: String = "",
    @ColumnInfo(name = DataCollectionEntry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = DataCollectionEntry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null,
) : Parcelable {

    @Ignore
    var assetStr: String = assetDescription.orEmpty()
        get() = assetDescription.orEmpty()
        set(value) {
            assetDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var warehouseStr = warehouseDescription.orEmpty()
        get() = warehouseDescription.orEmpty()
        set(value) {
            warehouseDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var warehouseAreaStr = warehouseAreaDescription.orEmpty()
        get() = warehouseAreaDescription.orEmpty()
        set(value) {
            warehouseAreaDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    private var contentsRead: Boolean = false

    @Ignore
    private var mContents: ArrayList<DataCollectionContent> = arrayListOf()

    fun contents(): ArrayList<DataCollectionContent> {
        if (contentsRead) return mContents
        else {
            mContents = ArrayList(DataCollectionContentRepository().selectByDataCollectionId(id))
            contentsRead = true
            return mContents
        }
    }

    constructor(parcel: Parcel) : this(
        dataCollectionId = parcel.readLong(),
        assetId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaId = parcel.readValue(Long::class.java.classLoader) as? Long,
        userId = parcel.readLong(),
        dateStart = parcel.readLong().let { if (it == -1L) null else Date(it) },
        dateEnd = parcel.readLong().let { if (it == -1L) null else Date(it) },
        completed = parcel.readInt(),
        transferredDate = parcel.readLong().let { if (it == -1L) null else Date(it) },
        id = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        assetDescription = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        warehouseDescription = parcel.readString().orEmpty(),
        warehouseAreaDescription = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionId)
        parcel.writeValue(assetId)
        parcel.writeValue(warehouseId)
        parcel.writeValue(warehouseAreaId)
        parcel.writeLong(userId)
        parcel.writeLong(dateStart?.time ?: -1L)
        parcel.writeLong(dateEnd?.time ?: -1L)
        parcel.writeInt(completed)
        parcel.writeLong(transferredDate?.time ?: -1L)
        parcel.writeLong(id)
        parcel.writeLong(routeProcessId)
        parcel.writeString(assetDescription)
        parcel.writeString(assetCode)
        parcel.writeString(warehouseDescription)
        parcel.writeString(warehouseAreaDescription)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataCollection

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<DataCollection> {
        override fun createFromParcel(parcel: Parcel): DataCollection {
            return DataCollection(parcel)
        }

        override fun newArray(size: Int): Array<DataCollection?> {
            return arrayOfNulls(size)
        }
    }
}

