package com.dacosys.assetControl.data.room.dto.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.repository.route.RouteProcessContentRepository

data class RouteProcessContent(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) var position: Int = 0,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_STATUS_ID) var routeProcessStatusId: Int = 0,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long? = null,
    @ColumnInfo(name = Entry.ASSET_STR) var assetStr: String? = null,
    @ColumnInfo(name = Entry.ASSET_CODE) var assetCode: String? = null,
    @ColumnInfo(name = Entry.ROUTE_ID) var routeId: Long? = null,
    @ColumnInfo(name = Entry.ROUTE_STR) var routeStr: String? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_STR) var warehouseStr: String? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_STR) var warehouseAreaStr: String? = null
) : Parcelable {

    fun saveChanges() = RouteProcessContentRepository().update(this)

    @Ignore
    val assetDescription: String = assetStr.orEmpty()

    @Ignore
    val code: String = assetCode.orEmpty()

    @Ignore
    val routeDescription: String = routeStr.orEmpty()

    @Ignore
    val warehouseDescription: String = warehouseStr.orEmpty()

    @Ignore
    val warehouseAreaDescription: String = warehouseAreaStr.orEmpty()

    @Ignore
    val routeProcessStatusStr: String = RouteProcessStatus.getById(routeProcessStatusId).description

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        dataCollectionRuleId = parcel.readLong(),
        level = parcel.readInt(),
        position = parcel.readInt(),
        routeProcessStatusId = parcel.readInt(),
        dataCollectionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        assetId = parcel.readValue(Long::class.java.classLoader) as? Long,
        assetStr = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        routeId = parcel.readValue(Long::class.java.classLoader) as? Long,
        routeStr = parcel.readString().orEmpty(),
        warehouseId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseStr = parcel.readString().orEmpty(),
        warehouseAreaId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "route_process_content"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val DATA_COLLECTION_RULE_ID = "data_collection_rule_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val ROUTE_PROCESS_STATUS_ID = "route_process_status_id"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val ID = "_id"

        const val ASSET_ID = "asset_id"
        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val ROUTE_ID = "route_id"
        const val ROUTE_STR = "route_str"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(routeProcessId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeInt(routeProcessStatusId)
        parcel.writeValue(dataCollectionId)
        parcel.writeValue(assetId)
        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeValue(routeId)
        parcel.writeString(routeStr)
        parcel.writeValue(warehouseId)
        parcel.writeString(warehouseStr)
        parcel.writeValue(warehouseAreaId)
        parcel.writeString(warehouseAreaStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessContent> {
        override fun createFromParcel(parcel: Parcel): RouteProcessContent {
            return RouteProcessContent(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessContent?> {
            return arrayOfNulls(size)
        }
    }
}