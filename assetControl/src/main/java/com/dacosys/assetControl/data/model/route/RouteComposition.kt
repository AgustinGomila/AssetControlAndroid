package com.dacosys.assetControl.data.model.route

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.EXPRESSION
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.FALSE_RESULT
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.LEVEL
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.POSITION
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.TRUE_RESULT
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionContract.RouteCompositionEntry.Companion.WAREHOUSE_ID

class RouteComposition : Parcelable {
    constructor(
        routeId: Long,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        assetId: Long,
        warehouseId: Long,
        warehouseAreaId: Long,
        expression: String?,
        trueResult: Int,
        falseResult: Int,
    ) {
        this.routeId = routeId
        this.dataCollectionRuleId = dataCollectionRuleId
        this.level = level
        this.position = position
        this.assetId = assetId
        this.warehouseId = warehouseId
        this.warehouseAreaId = warehouseAreaId
        this.expression = expression
        this.trueResult = trueResult
        this.falseResult = falseResult
    }

    var routeId: Long = 0
    var dataCollectionRuleId: Long = 0
    var level: Int = 0
    var position: Int = 0
    var assetId: Long = 0
    var warehouseId: Long = 0
    var warehouseAreaId: Long = 0
    var expression: String? = null
    var trueResult: Int = 0
    var falseResult: Int = 0

    constructor(parcel: android.os.Parcel) {
        routeId = parcel.readLong()
        dataCollectionRuleId = parcel.readLong()
        level = parcel.readInt()
        position = parcel.readInt()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        expression = parcel.readString()
        trueResult = parcel.readInt()
        falseResult = parcel.readInt()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(ROUTE_ID, routeId)
        values.put(DATA_COLLECTION_RULE_ID, dataCollectionRuleId)
        values.put(LEVEL, level)
        values.put(POSITION, position)
        values.put(ASSET_ID, assetId)
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        values.put(EXPRESSION, expression)
        values.put(TRUE_RESULT, trueResult)
        values.put(FALSE_RESULT, falseResult)

        return values
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(routeId)
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeInt(level)
        parcel.writeInt(position)
        parcel.writeLong(assetId)
        parcel.writeLong(warehouseId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeString(expression)
        parcel.writeInt(trueResult)
        parcel.writeInt(falseResult)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteComposition> {
        override fun createFromParcel(parcel: android.os.Parcel): RouteComposition {
            return RouteComposition(parcel)
        }

        override fun newArray(size: Int): Array<RouteComposition?> {
            return arrayOfNulls(size)
        }
    }
}