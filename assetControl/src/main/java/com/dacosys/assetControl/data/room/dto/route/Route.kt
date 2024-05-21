package com.dacosys.assetControl.data.room.dto.route

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.room.repository.route.RouteCompositionRepository
import com.dacosys.assetControl.data.webservice.route.RouteObject
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetString

class Route(
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0
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

    object Entry {
        const val TABLE_NAME = "route"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
    }

    override fun describeContents(): Int {
        return 0
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
            val prefix = prefsGetString(Preference.acFilterRouteDescription)
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
