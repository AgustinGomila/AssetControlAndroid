package com.dacosys.assetControl.model.routes.routeProcess.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.COLLECTOR_ROUTE_PROCESS_ID
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.COMPLETED
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_PROCESS_DATE
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.TRANSFERED
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract.RouteProcessEntry.Companion.USER_ID
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessDbHelper
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper

class RouteProcess : Parcelable {
    var collectorRouteProcessId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        userId: Long,
        routeId: Long,
        routeProcessDate: String,
        completed: Boolean,
        transferred: Boolean,
        transferredDate: String?,
        routeProcessId: Long?,
        collectorRouteProcessId: Long,
    ) {
        this.userId = userId
        this.routeId = routeId
        this.routeProcessDate = routeProcessDate
        this.completed = completed
        this.transfered = transferred
        this.transferedDate = transferredDate
        this.routeProcessId = routeProcessId
        this.collectorRouteProcessId = collectorRouteProcessId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        collectorRouteProcessId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = RouteProcessDbHelper().selectById(this.collectorRouteProcessId)

        dataRead = true
        return when {
            temp != null -> {
                this.userId = temp.userId
                this.routeId = temp.routeId
                this.routeProcessDate = temp.routeProcessDate
                this.completed = temp.completed
                this.transfered = temp.transfered
                this.transferedDate = temp.transferedDate
                this.routeProcessId = temp.routeProcessId
                this.collectorRouteProcessId = temp.collectorRouteProcessId

                this.userStr = temp.userStr
                this.routeStr = temp.routeStr

                true
            }
            else -> false
        }
    }

    val contents: ArrayList<RouteProcessContent>
        get() {
            return RouteProcessContentDbHelper().selectByCollectorRouteProcessId(
                this.collectorRouteProcessId
            )
        }

    var routeId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var routeProcessId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var routeProcessDate: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var transferedDate: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var completed: Boolean = false
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return false
                }
            }
            return field
        }

    var transfered: Boolean = false
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return false
                }
            }
            return field
        }

    var userId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var userStr: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    var routeStr: String = ""
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return ""
                }
            }
            return field
        }

    constructor(parcel: android.os.Parcel) {
        userId = parcel.readLong()
        routeId = parcel.readLong()
        routeProcessDate = parcel.readString() ?: ""
        completed = parcel.readByte() != 0.toByte()
        transfered = parcel.readByte() != 0.toByte()
        transferedDate = parcel.readString()
        routeProcessId = parcel.readLong()
        collectorRouteProcessId = parcel.readLong()

        userStr = parcel.readString() ?: ""
        routeStr = parcel.readString() ?: ""

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(USER_ID, userId)
        values.put(ROUTE_ID, routeId)
        values.put(ROUTE_PROCESS_DATE, routeProcessDate)
        values.put(COMPLETED, completed)
        values.put(TRANSFERED, transfered)
        values.put(TRANSFERED_DATE, transferedDate)
        values.put(ROUTE_PROCESS_ID, routeProcessId)
        values.put(COLLECTOR_ROUTE_PROCESS_ID, collectorRouteProcessId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return RouteProcessDbHelper().update(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is RouteProcess) {
            false
        } else equals(this.collectorRouteProcessId, other.collectorRouteProcessId)
    }

    override fun hashCode(): Int {
        return this.collectorRouteProcessId.hashCode()
    }

    class CustomComparator : Comparator<RouteProcess> {
        override fun compare(o1: RouteProcess, o2: RouteProcess): Int {
            if (o1.collectorRouteProcessId < o2.collectorRouteProcessId) {
                return -1
            } else if (o1.collectorRouteProcessId > o2.collectorRouteProcessId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(routeId)
        parcel.writeString(routeProcessDate)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeByte(if (transfered) 1 else 0)
        parcel.writeString(transferedDate)
        parcel.writeLong(if (routeProcessId == null) 0L else routeProcessId ?: return)
        parcel.writeLong(collectorRouteProcessId)

        parcel.writeString(userStr)
        parcel.writeString(routeStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcess> {
        override fun createFromParcel(parcel: android.os.Parcel): RouteProcess {
            return RouteProcess(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcess?> {
            return arrayOfNulls(size)
        }

        fun add(
            userId: Long,
            routeId: Long,
            routeProcessDate: String,
            completed: Boolean,
            transferred: Boolean,
            transferredDate: String?,
            routeProcessId: Long?,
            collectorRouteProcessId: Long,
        ): RouteProcess? {
            val i = RouteProcessDbHelper()
            val newId = i.insert(
                userId,
                routeId,
                routeProcessDate,
                completed,
                transferred,
                transferredDate,
                routeProcessId,
                collectorRouteProcessId
            )
            return if (newId < 1) null else i.selectById(newId)
        }
    }
}