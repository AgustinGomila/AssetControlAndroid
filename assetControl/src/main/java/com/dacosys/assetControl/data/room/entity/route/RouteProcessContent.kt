package com.dacosys.assetControl.data.room.entity.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent.Entry
import com.dacosys.assetControl.data.room.repository.route.RouteProcessContentRepository

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ROUTE_PROCESS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_ID}"
        ),
        Index(
            value = [Entry.LEVEL],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}"
        ),
        Index(
            value = [Entry.POSITION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.POSITION}"
        ),
        Index(
            value = [Entry.ROUTE_PROCESS_STATUS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_STATUS_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        )
    ]
)
data class RouteProcessContent(
    @PrimaryKey @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) var position: Int = 0,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_STATUS_ID) var routeProcessStatusId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @Ignore var assetId: Long? = null,
    @Ignore var assetStr: String = "",
    @Ignore var assetCode: String = "",
    @Ignore var routeId: Long? = null,
    @Ignore var routeStr: String = "",
    @Ignore var warehouseId: Long? = null,
    @Ignore var warehouseStr: String = "",
    @Ignore var warehouseAreaId: Long? = null,
    @Ignore var warehouseAreaStr: String = "",
) : Parcelable {

    fun saveChanges() = RouteProcessContentRepository().update(this)

    @Ignore
    var processStatusId: Int = routeProcessStatusId.toInt()
        set(value) {
            routeProcessStatusId = value.toLong()
            field = value
        }

    @Ignore
    val routeProcessStatusStr: String = RouteProcessStatus.getById(processStatusId).description

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        dataCollectionRuleId = parcel.readLong(),
        level = parcel.readInt(),
        position = parcel.readInt(),
        routeProcessStatusId = parcel.readLong(),
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
        const val ID = "route_process_content_id"

        const val ASSET_ID = "asset_id"
        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val ROUTE_ID = "route_id"
        const val ROUTE_STR = "route_str"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
        const val ROUTE_PROCESS_STATUS_STR = "route_process_status_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(routeProcessId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(routeProcessStatusId)
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
