package com.dacosys.assetControl.model.routes.routeProcessContent.`object`

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.LEVEL
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.POSITION
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_CONTENT_ID
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_STATUS_ID
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcessStatus.`object`.RouteProcessStatus

class RouteProcessContent : Parcelable {
    var routeProcessContentId: Long = 0
    private var dataRead: Boolean = false

    fun setDataRead() {
        this.dataRead = true
    }

    constructor(
        routeProcessId: Long,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        routeProcessStatusId: Int,
        dataCollectionId: Long?,
        routeProcessContentId: Long,
    ) {
        this.routeProcessId = routeProcessId
        this.dataCollectionRuleId = dataCollectionRuleId
        this.level = level
        this.position = position
        this.routeProcessStatusId = routeProcessStatusId
        this.dataCollectionId = dataCollectionId
        this.routeProcessContentId = routeProcessContentId

        dataRead = true
    }

    constructor(id: Long, doChecks: Boolean) {
        routeProcessContentId = id

        if (doChecks) {
            refreshData()
        }
    }

    private fun refreshData(): Boolean {
        val temp = RouteProcessContentDbHelper().selectById(this.routeProcessContentId)

        dataRead = true
        return when {
            temp != null -> {
                this.routeProcessId = temp.routeProcessId
                this.dataCollectionRuleId = temp.dataCollectionRuleId
                this.level = temp.level
                this.position = temp.position
                this.routeProcessStatusId = temp.routeProcessStatusId
                this.dataCollectionId = temp.dataCollectionId
                this.routeProcessContentId = temp.routeProcessContentId

                this.assetId = temp.assetId
                this.assetStr = temp.assetStr
                this.assetCode = temp.assetCode
                this.warehouseStr = temp.warehouseStr
                this.warehouseAreaStr = temp.warehouseAreaStr
                this.routeId = temp.routeId
                this.routeStr = routeStr

                //this.routeProcessStatusStr = temp.routeProcessStatusStr

                true
            }
            else -> false
        }
    }

    /*
    val routeProcess: RouteProcess?
        get() {
            return if (routeProcessId == 0L) {
                null
            } else RouteProcess( routeProcessId, false)
        }
    */

    var routeProcessId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    /*
    val dataCollectionRule: DataCollectionRule?
        get() {
            return if (dataCollectionRuleId == 0L) {
                null
            } else DataCollectionRule( dataCollectionRuleId, false)
        }
    */

    var dataCollectionRuleId: Long = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    /*
    val dataCollection: DataCollection?
        get() {
            return if (dataCollectionId == null || dataCollectionId == 0L) {
                null
            } else DataCollection( dataCollectionId!!, false)
        }
    */

    var dataCollectionId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    val routeProcessStatusStr: String
        get() {
            return RouteProcessStatus.getById(routeProcessStatusId)!!.description
        }

    var routeProcessStatusId: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var position: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var level: Int = 0
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return 0
                }
            }
            return field
        }

    var assetId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var assetStr: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var assetCode: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var warehouseAreaId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var warehouseAreaStr: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var warehouseId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var warehouseStr: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var routeId: Long? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    var routeStr: String? = null
        get() {
            if (!dataRead) {
                if (!refreshData()) {
                    return null
                }
            }
            return field
        }

    constructor(parcel: android.os.Parcel) {
        this.routeProcessId = parcel.readLong()
        this.dataCollectionRuleId = parcel.readLong()
        this.level = parcel.readInt()
        this.position = parcel.readInt()
        this.routeProcessStatusId = parcel.readInt()
        this.dataCollectionId = parcel.readLong()
        this.routeProcessContentId = parcel.readLong()

        this.assetId = parcel.readLong()
        this.assetStr = parcel.readString()
        this.assetCode = parcel.readString()
        this.warehouseId = parcel.readLong()
        this.warehouseStr = parcel.readString()
        this.warehouseAreaId = parcel.readLong()
        this.warehouseAreaStr = parcel.readString()
        this.routeId = parcel.readLong()
        this.routeStr = parcel.readString()

        dataRead = parcel.readByte() != 0.toByte()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(ROUTE_PROCESS_ID, routeProcessId)
        values.put(DATA_COLLECTION_RULE_ID, dataCollectionRuleId)
        values.put(LEVEL, level)
        values.put(POSITION, position)
        values.put(ROUTE_PROCESS_STATUS_ID, routeProcessStatusId)
        values.put(DATA_COLLECTION_ID, dataCollectionId)
        values.put(ROUTE_PROCESS_CONTENT_ID, routeProcessContentId)

        return values
    }

    fun saveChanges(): Boolean {
        if (!dataRead) {
            if (!refreshData()) {
                return false
            }
        }
        return RouteProcessContentDbHelper().updateStatus(this)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is RouteProcessContent) {
            false
        } else equals(this.routeProcessContentId, other.routeProcessContentId)
    }

    override fun hashCode(): Int {
        return this.routeProcessContentId.hashCode()
    }

    class CustomComparator : Comparator<RouteProcessContent> {
        override fun compare(o1: RouteProcessContent, o2: RouteProcessContent): Int {
            if (o1.routeProcessContentId < o2.routeProcessContentId) {
                return -1
            } else if (o1.routeProcessContentId > o2.routeProcessContentId) {
                return 1
            }
            return 0
        }
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(routeProcessId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeInt(routeProcessStatusId)
        parcel.writeLong(if (dataCollectionId == null) 0L else dataCollectionId ?: return)
        parcel.writeLong(routeProcessContentId)

        parcel.writeLong(if (assetId == null) 0L else assetId ?: return)
        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeLong(if (warehouseId == null) 0L else warehouseId ?: return)
        parcel.writeString(warehouseStr)
        parcel.writeLong(if (warehouseAreaId == null) 0L else warehouseAreaId ?: return)
        parcel.writeString(warehouseAreaStr)
        parcel.writeLong(if (routeId == null) 0L else routeId ?: return)
        parcel.writeString(routeStr)

        parcel.writeByte(if (dataRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteProcessContent> {
        override fun createFromParcel(parcel: android.os.Parcel): RouteProcessContent {
            return RouteProcessContent(parcel)
        }

        override fun newArray(size: Int): Array<RouteProcessContent?> {
            return arrayOfNulls(size)
        }
    }
}