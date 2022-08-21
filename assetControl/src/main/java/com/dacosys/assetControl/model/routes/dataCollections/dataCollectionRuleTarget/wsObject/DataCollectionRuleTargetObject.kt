package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class DataCollectionRuleTargetObject() : Parcelable {
    var dataCollectionRuleId = 0L
    var assetId: Long? = null
    var warehouseId: Long? = null
    var warehouseAreaId: Long? = null
    var itemCategoryId: Long? = null

    constructor(parcel: Parcel) : this() {
        dataCollectionRuleId = parcel.readLong()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        itemCategoryId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): DataCollectionRuleTargetObject {
        val x = DataCollectionRuleTargetObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "data_collection_rule_id" -> {
                            x.dataCollectionRuleId = soValue as? Long ?: 0L
                        }
                        "asset_id" -> {
                            x.assetId = soValue as? Long ?: 0L
                        }
                        "warehouse_id" -> {
                            x.warehouseId = soValue as? Long ?: 0L
                        }
                        "warehouse_area_id" -> {
                            x.warehouseAreaId = soValue as? Long ?: 0L
                        }
                        "item_category_id" -> {
                            x.itemCategoryId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeLong(if (assetId == null) 0L else assetId ?: return)
        parcel.writeLong(if (warehouseId == null) 0L else warehouseId ?: return)
        parcel.writeLong(if (warehouseAreaId == null) 0L else warehouseAreaId ?: return)
        parcel.writeLong(if (itemCategoryId == null) 0L else itemCategoryId ?: return)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRuleTargetObject> {
        override fun createFromParcel(parcel: Parcel): DataCollectionRuleTargetObject {
            return DataCollectionRuleTargetObject(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRuleTargetObject?> {
            return arrayOfNulls(size)
        }
    }
}


