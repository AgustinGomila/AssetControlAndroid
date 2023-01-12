package com.dacosys.assetControl.model.datacollection

import android.content.ContentValues
import android.os.Parcelable
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.ASSET_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleTargetDbHelper
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.location.Warehouse
import com.dacosys.assetControl.model.location.WarehouseArea

class DataCollectionRuleTarget : Parcelable {
    constructor(
        dataCollectionRuleId: Long,
        assetId: Long?,
        warehouseId: Long?,
        warehouseAreaId: Long?,
        itemCategoryId: Long?,
    ) {
        this.dataCollectionRuleId = dataCollectionRuleId
        this.assetId = assetId
        this.warehouseId = warehouseId
        this.warehouseAreaId = warehouseAreaId
        this.itemCategoryId = itemCategoryId
    }

    val dataCollectionRule: DataCollectionRule
        get() {
            return DataCollectionRule(dataCollectionRuleId, false)
        }

    val asset: Asset?
        get() {
            return if (assetId != null)
                Asset(assetId!!, false)
            else
                null
        }

    val itemCategory: ItemCategory?
        get() {
            return if (itemCategoryId != null)
                ItemCategory(itemCategoryId!!, false)
            else
                null
        }

    val warehouseArea: WarehouseArea?
        get() {
            return if (warehouseAreaId != null)
                WarehouseArea(warehouseAreaId!!, false)
            else
                null
        }

    val warehouse: Warehouse?
        get() {
            return if (warehouseId != null)
                Warehouse(warehouseId!!, false)
            else
                null
        }

    var dataCollectionRuleId: Long = 0
    var assetId: Long? = null
    var warehouseId: Long? = null
    var warehouseAreaId: Long? = null
    var itemCategoryId: Long? = null

    constructor(parcel: android.os.Parcel) {
        dataCollectionRuleId = parcel.readLong()
        assetId = parcel.readLong()
        warehouseId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        itemCategoryId = parcel.readLong()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(DATA_COLLECTION_RULE_ID, dataCollectionRuleId)
        values.put(ITEM_CATEGORY_ID, itemCategoryId)
        values.put(ASSET_ID, assetId)
        values.put(WAREHOUSE_ID, warehouseId)
        values.put(WAREHOUSE_AREA_ID, warehouseAreaId)
        return values
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeLong(dataCollectionRuleId)
        parcel.writeLong(if (assetId == null) 0L else assetId ?: return)
        parcel.writeLong(if (warehouseId == null) 0L else warehouseId ?: return)
        parcel.writeLong(if (warehouseAreaId == null) 0L else warehouseAreaId ?: return)
        parcel.writeLong(if (itemCategoryId == null) 0L else itemCategoryId ?: return)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionRuleTarget> {
        override fun createFromParcel(parcel: android.os.Parcel): DataCollectionRuleTarget {
            return DataCollectionRuleTarget(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionRuleTarget?> {
            return arrayOfNulls(size)
        }

        fun add(
            dataCollectionRuleId: Long,
            assetId: Long?,
            warehouseId: Long?,
            warehouseAreaId: Long?,
            itemCategoryId: Long?,
        ): Boolean {
            val i = DataCollectionRuleTargetDbHelper()
            return i.insert(
                dataCollectionRuleId,
                assetId,
                warehouseId,
                warehouseAreaId,
                itemCategoryId
            )
        }
    }
}