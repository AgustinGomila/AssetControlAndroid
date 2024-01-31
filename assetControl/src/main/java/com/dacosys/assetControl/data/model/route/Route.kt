package com.dacosys.assetControl.data.model.route

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteContract.RouteEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.route.RouteContract.RouteEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.route.RouteContract.RouteEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteDbHelper
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.settings.Preference

class Route : Parcelable {
    var routeId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        routeId: Long,
        description: String,
        active: Boolean,
    ) {
        this.routeId = routeId
        this.description = description
        this.active = active

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        routeId = id
        if (doChecks) refreshData()
    }

    private fun refreshData(): Boolean {
        val temp = RouteDbHelper().selectById(this.routeId)

        dataRead = true
        return when {
            temp != null -> {
                routeId = temp.routeId
                active = temp.active
                description = temp.description

                true
            }

            else -> false
        }
    }

    override fun toString(): String {
        return description
    }

    val composition: ArrayList<RouteComposition>
        get() {
            return RouteCompositionDbHelper().selectByRouteId(this.routeId)
        }

    var description: String = ""
        get() {
            return if (!dataRead && !refreshData()) ""
            else field
        }

    var active: Boolean = false
        get() {
            return if (!dataRead && !refreshData()) false
            else field
        }

    constructor(parcel: android.os.Parcel) {
        routeId = parcel.readLong()
        active = parcel.readByte() != 0.toByte()
        description = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ROUTE_ID, routeId)
        values.put(DESCRIPTION, description)
        values.put(ACTIVE, active)
        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return RouteDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Route) {
            false
        } else equals(this.routeId, other.routeId)
    }

    override fun hashCode(): Int {
        return this.routeId.hashCode()
    }

    class CustomComparator : Comparator<Route> {
        override fun compare(o1: Route, o2: Route): Int {
            if (o1.routeId < o2.routeId) {
                return -1
            } else if (o1.routeId > o2.routeId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(routeId)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(description)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Route> {
        override fun createFromParcel(parcel: android.os.Parcel): Route {
            return Route(parcel)
        }

        override fun newArray(size: Int): Array<Route?> {
            return arrayOfNulls(size)
        }

        fun getAvailableRoutes(routes: ArrayList<Route>): ArrayList<Route> {
            val result: ArrayList<Route> = ArrayList()
            val prefix =
                prefsGetString(Preference.acFilterRouteDescription)
            val prefixes = prefix.split(";", ignoreCase = true, limit = 0).toTypedArray()

            var validPrefix = false
            for (p in prefixes) {
                if (p.isEmpty()) continue
                validPrefix = true
                break
            }

            if (routes.size > 0) {
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