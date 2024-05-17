package com.dacosys.assetControl.data.room.entity.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.route.RouteProcess.Entry
import com.dacosys.assetControl.data.room.repository.route.RouteProcessContentRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(value = [Entry.ROUTE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_ID}"),
        Index(value = [Entry.ROUTE_PROCESS_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ROUTE_PROCESS_ID}")
    ]
)
data class RouteProcess(
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_ID) var routeId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_DATE) var routeProcessDate: Date = Date(),
    @ColumnInfo(name = Entry.COMPLETED) var mCompleted: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @PrimaryKey @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @Ignore var routeStr: String = "",
) : Parcelable {

    fun saveChanges() = RouteProcessRepository().update(this)

    @Ignore
    var completed: Boolean = mCompleted == 1
        set(value) {
            mCompleted = if (value) 1 else 0
            field = value
        }

    @Ignore
    private var contentsRead: Boolean = false

    @Ignore
    private var mContents: ArrayList<RouteProcessContent> = arrayListOf()

    fun contents(): ArrayList<RouteProcessContent> {
        if (contentsRead) return mContents
        else {
            mContents = ArrayList(RouteProcessContentRepository().selectByRouteProcessId(id))
            contentsRead = true
            return mContents
        }
    }

    constructor(parcel: Parcel) : this(
        userId = parcel.readLong(),
        routeId = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        routeProcessDate = parcel.readString().orEmpty().toDate(),
        mCompleted = parcel.readInt(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        transferredDate = parcel.readString().orEmpty().toDate(),
        id = parcel.readLong(),
        routeStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "route_process"
        const val USER_ID = "user_id"
        const val ROUTE_ID = "route_id"
        const val ROUTE_PROCESS_DATE = "route_process_date"
        const val COMPLETED = "completed"
        const val TRANSFERRED = "transfered"
        const val TRANSFERRED_DATE = "transfered_date"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val ID = "_id"

        const val ROUTE_STR = "route_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(routeId)
        parcel.writeLong(routeProcessId)
        parcel.writeString(routeProcessDate.toString())
        parcel.writeInt(mCompleted)
        parcel.writeValue(transferred)
        parcel.writeString(transferredDate?.toString())
        parcel.writeLong(id)
        parcel.writeString(routeStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcess> {
        override fun createFromParcel(parcel: Parcel): RouteProcess {
            return RouteProcess(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcess?> {
            return arrayOfNulls(size)
        }
    }
}