package com.example.assetControl.data.room.dto.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.data.room.repository.route.RouteCompositionRepository
import com.example.assetControl.data.webservice.route.RouteObject

abstract class RouteEntry {
    companion object {
        const val TABLE_NAME = "route"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }
}

class Route(
    @ColumnInfo(name = RouteEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = RouteEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = RouteEntry.ACTIVE) val active: Int = 0
) : Parcelable {

    override fun toString(): String {
        return description
    }

    @Ignore
    private var compositionRead: Boolean = false

    @Ignore
    private var mComposition: ArrayList<RouteComposition> = arrayListOf()

    fun composition(): ArrayList<RouteComposition> {
        if (compositionRead) return mComposition
        else {
            mComposition = ArrayList(RouteCompositionRepository().selectByRouteId(id))
            compositionRead = true
            return mComposition
        }
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt()
    )

    constructor(rObj: RouteObject) : this(
        id = rObj.route_id,
        description = rObj.description,
        active = rObj.active
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Route> {
        override fun createFromParcel(parcel: Parcel): Route {
            return Route(parcel)
        }

        override fun newArray(size: Int): Array<Route?> {
            return arrayOfNulls(size)
        }

        fun getAvailableRoutes(routes: List<Route>): ArrayList<Route> {
            val result: ArrayList<Route> = ArrayList()
            val prefix = svm.acFilterRouteDescription
            val prefixes = prefix.split(";", ignoreCase = true, limit = 0).toTypedArray()

            var validPrefix = false
            for (p in prefixes) {
                if (p.isEmpty()) continue
                validPrefix = true
                break
            }

            if (routes.isNotEmpty()) {
                if (!validPrefix) {
                    result.addAll(routes)
                } else {
                    for (t in routes) {
                        for (p in prefixes) {
                            if (p.isEmpty()) continue
                            if (t.description.contains(p, ignoreCase = true)) {
                                result.add(t)
                            }
                        }
                    }
                }
            }

            return result
        }
    }
}
