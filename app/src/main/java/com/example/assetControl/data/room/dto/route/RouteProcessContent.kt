package com.example.assetControl.data.room.dto.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.example.assetControl.data.enums.route.RouteProcessStatus
import com.example.assetControl.data.room.repository.route.RouteProcessContentRepository

abstract class RouteProcessContentEntry {
    companion object {
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
}

data class RouteProcessContent(
    @ColumnInfo(name = RouteProcessContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = RouteProcessContentEntry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = RouteProcessContentEntry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = RouteProcessContentEntry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = RouteProcessContentEntry.POSITION) var position: Int = 0,
    @ColumnInfo(name = RouteProcessContentEntry.ROUTE_PROCESS_STATUS_ID) var routeProcessStatusId: Int = 0,
    @ColumnInfo(name = RouteProcessContentEntry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = RouteProcessContentEntry.ASSET_ID) var assetId: Long? = null,
    @ColumnInfo(name = RouteProcessContentEntry.ASSET_STR) var assetDescription: String? = null,
    @ColumnInfo(name = RouteProcessContentEntry.ASSET_CODE) var code: String? = null,
    @ColumnInfo(name = RouteProcessContentEntry.ROUTE_ID) var routeId: Long? = null,
    @ColumnInfo(name = RouteProcessContentEntry.ROUTE_STR) var routeDescription: String? = null,
    @ColumnInfo(name = RouteProcessContentEntry.WAREHOUSE_ID) var warehouseId: Long? = null,
    @ColumnInfo(name = RouteProcessContentEntry.WAREHOUSE_STR) var warehouseDescription: String? = null,
    @ColumnInfo(name = RouteProcessContentEntry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long? = null,
    @ColumnInfo(name = RouteProcessContentEntry.WAREHOUSE_AREA_STR) var warehouseAreaDescription: String? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteProcessContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun saveChanges() = RouteProcessContentRepository().update(this)

    @Ignore
    var assetStr: String = assetDescription.orEmpty()
        get() = assetDescription.orEmpty()
        set(value) {
            assetDescription = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var assetCode: String = code.orEmpty()
        get() = code.orEmpty()
        set(value) {
            code = value.ifEmpty { null }
            field = value
        }

    @Ignore
    var routeStr: String = routeDescription.orEmpty()
        get() = routeDescription.orEmpty()
        set(value) {
            routeDescription = value.ifEmpty { null }
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
    var status: RouteProcessStatus = RouteProcessStatus.getById(routeProcessStatusId)
        get() = RouteProcessStatus.getById(routeProcessStatusId)
        set(value) {
            routeProcessStatusId = value.id
            field = value
        }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        dataCollectionRuleId = parcel.readLong(),
        level = parcel.readInt(),
        position = parcel.readInt(),
        routeProcessStatusId = parcel.readInt(),
        dataCollectionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        assetId = parcel.readValue(Long::class.java.classLoader) as? Long,
        assetDescription = parcel.readString().orEmpty(),
        code = parcel.readString().orEmpty(),
        routeId = parcel.readValue(Long::class.java.classLoader) as? Long,
        routeDescription = parcel.readString().orEmpty(),
        warehouseId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseDescription = parcel.readString().orEmpty(),
        warehouseAreaId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaDescription = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(routeProcessId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeInt(routeProcessStatusId)
        parcel.writeValue(dataCollectionId)
        parcel.writeValue(assetId)
        parcel.writeString(assetDescription)
        parcel.writeString(code)
        parcel.writeValue(routeId)
        parcel.writeString(routeDescription)
        parcel.writeValue(warehouseId)
        parcel.writeString(warehouseDescription)
        parcel.writeValue(warehouseAreaId)
        parcel.writeString(warehouseAreaDescription)
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