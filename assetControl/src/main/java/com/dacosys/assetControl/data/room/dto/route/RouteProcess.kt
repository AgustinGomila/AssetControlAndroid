package com.dacosys.assetControl.data.room.dto.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.room.repository.route.RouteProcessContentRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
import java.util.*

class RouteProcess(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_ID) var routeId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_ID) var routeProcessId: Long = 0L,
    @ColumnInfo(name = Entry.ROUTE_PROCESS_DATE) var routeProcessDate: Date = Date(),
    @ColumnInfo(name = Entry.COMPLETED) var mCompleted: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.ROUTE_STR) var routeStr: String? = null,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteProcess

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

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
        id = parcel.readLong(),
        userId = parcel.readLong(),
        routeId = parcel.readLong(),
        routeProcessId = parcel.readLong(),
        routeProcessDate = parcel.readString().orEmpty().toDate(),
        mCompleted = parcel.readInt(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        transferredDate = parcel.readString().orEmpty().toDate(),
        routeStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "route_process"
        const val USER_ID = "user_id"
        const val ROUTE_ID = "route_id"
        const val ROUTE_PROCESS_DATE = "route_process_date"
        const val COMPLETED = "completed"
        const val TRANSFERRED = "transferred"
        const val TRANSFERRED_DATE = "transferred_date"
        const val ROUTE_PROCESS_ID = "route_process_id"
        const val ID = "_id"

        const val ROUTE_STR = "route_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(userId)
        parcel.writeLong(routeId)
        parcel.writeLong(routeProcessId)
        parcel.writeString(routeProcessDate.toString())
        parcel.writeInt(mCompleted)
        parcel.writeValue(transferred)
        parcel.writeString(transferredDate?.toString())
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