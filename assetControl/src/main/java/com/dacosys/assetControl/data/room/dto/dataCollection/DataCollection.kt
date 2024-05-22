package com.dacosys.assetControl.data.room.dto.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionContentRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

class DataCollection(
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.DATE_START) var dateStart: Date? = null,
    @ColumnInfo(name = Entry.DATE_END) var dateEnd: Date? = null,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.COLLECTOR_ROUTE_PROCESS_ID) var collectorRouteProcessId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_STR) var assetDescription: String? = null,
    @ColumnInfo(name = Entry.ASSET_CODE) var assetCode: String = "",
    @ColumnInfo(name = Entry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null,
) : Parcelable {

    @Ignore
    val assetStr = assetDescription.orEmpty()

    @Ignore
    val warehouseStr = warehouseDescription.orEmpty()

    @Ignore
    val warehouseAreaStr = warehouseAreaDescription.orEmpty()

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
        dateStart = parcel.readString().orEmpty().toDate(),
        dateEnd = parcel.readString().orEmpty().toDate(),
        completed = parcel.readInt(),
        transferredDate = parcel.readString().orEmpty().toDate(),
        id = parcel.readLong(),
        collectorRouteProcessId = parcel.readLong(),
        assetDescription = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        warehouseDescription = parcel.readString().orEmpty(),
        warehouseAreaDescription = parcel.readString().orEmpty()
    )

    object Entry {
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
        const val COLLECTOR_ROUTE_PROCESS_ID = "collector_route_process_id"

        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionId)
        parcel.writeValue(assetId)
        parcel.writeValue(warehouseId)
        parcel.writeValue(warehouseAreaId)
        parcel.writeLong(userId)
        parcel.writeString(dateStart?.toString())
        parcel.writeString(dateEnd?.toString())
        parcel.writeInt(completed)
        parcel.writeString(transferredDate?.toString())
        parcel.writeLong(id)
        parcel.writeLong(collectorRouteProcessId)
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

