package com.dacosys.assetControl.model.routes.routeProcessStatus.`object`

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.routes.routeProcessStatus.dbHelper.RouteProcessStatusContract
import java.util.*

class RouteProcessStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(routeProcessStatusId: Int, description: String) {
        this.description = description
        this.id = routeProcessStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is RouteProcessStatus) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(RouteProcessStatusContract.RouteProcessStatusEntry.ROUTE_PROCESS_STATUS_ID, id)
        values.put(RouteProcessStatusContract.RouteProcessStatusEntry.DESCRIPTION, description)
        return values
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessStatus> {
        override fun createFromParcel(parcel: Parcel): RouteProcessStatus {
            return RouteProcessStatus(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessStatus?> {
            return arrayOfNulls(size)
        }

        var unknown = RouteProcessStatus(
            0,
            Statics.AssetControl.getContext().getString(R.string.route_process_status_unknown)
        )
        var processed = RouteProcessStatus(
            1,
            Statics.AssetControl.getContext().getString(R.string.route_process_status_processed)
        )
        var skipped = RouteProcessStatus(
            2,
            Statics.AssetControl.getContext().getString(R.string.route_process_status_skipped)
        )
        var notProcessed = RouteProcessStatus(
            3,
            Statics.AssetControl.getContext().getString(R.string.route_process_status_not_processed)
        )

        fun getAll(): ArrayList<RouteProcessStatus> {
            val allSections = ArrayList<RouteProcessStatus>()
            Collections.addAll(
                allSections,
                unknown,
                processed,
                skipped,
                notProcessed
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(routeProcessStatusId: Int): RouteProcessStatus? {
            return getAll().firstOrNull { it.id == routeProcessStatusId }
        }
    }
}