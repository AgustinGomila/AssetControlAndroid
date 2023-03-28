package com.dacosys.assetControl.model.route

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.LEVEL
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.POSITION
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.ROUTE_PROCESS_CONTENT_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.STEP
import com.dacosys.assetControl.model.dataCollection.DataCollection

class RouteProcessSteps : Parcelable {
    override fun describeContents(): Int {
        return step
    }

    constructor(
        routeProcessId: Long,
        routeProcessContentId: Long,
        level: Int,
        position: Int,
        dataCollectionId: Long?,
        step: Int,
    ) {
        this.routeProcessId = routeProcessId
        this.routeProcessContentId = routeProcessContentId
        this.level = level
        this.position = position
        this.dataCollectionId = dataCollectionId
        this.step = step
    }

    var routeProcessId: Long = 0

    val routeProcess: RouteProcess?
        get() {
            return if (routeProcessId == 0L) {
                null
            } else RouteProcess(routeProcessId, false)
        }

    var routeProcessContentId: Long = 0

    val routeContentProcess: RouteProcessContent?
        get() {
            return if (routeProcessContentId == 0L) {
                null
            } else RouteProcessContent(routeProcessContentId, false)
        }

    var dataCollectionId: Long? = null

    val dataCollection: DataCollection?
        get() {
            return if (dataCollectionId == null || dataCollectionId == 0L) {
                null
            } else DataCollection(dataCollectionId!!, false)
        }

    var step: Int = 0

    var level: Int = 0

    var position: Int = 0

    constructor(parcel: android.os.Parcel) {
        this.routeProcessId = parcel.readLong()
        this.routeProcessContentId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.dataCollectionId = parcel.readLong()
        this.step = parcel.readInt()
    }

    fun toStepsValues(): ContentValues {
        val values = ContentValues()

        values.put(ROUTE_PROCESS_ID, routeProcessId)
        values.put(ROUTE_PROCESS_CONTENT_ID, routeProcessContentId)
        values.put(LEVEL, level)
        values.put(POSITION, position)
        values.put(DATA_COLLECTION_ID, dataCollectionId)
        values.put(STEP, step)

        return values
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(routeProcessId)
        parcel.writeLong(routeProcessContentId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(if (dataCollectionId == null) 0L else dataCollectionId ?: return)
        parcel.writeInt(step)
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessSteps> {
        override fun createFromParcel(parcel: android.os.Parcel): RouteProcessSteps {
            return RouteProcessSteps(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessSteps?> {
            return arrayOfNulls(size)
        }
    }
}