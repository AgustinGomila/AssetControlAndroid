package com.dacosys.assetControl.model.reviews.assetReview.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class AssetReviewObject() : Parcelable {
    var assetReviewId: Long = 0
    var assetReviewDate = String()
    var obs = String()
    var userId: Long = 0
    var warehouseAreaId: Long = 0
    var warehouseId: Long = 0
    var modificationDate = String()
    var collectorAssetReviewId: Long = 0
    var statusId: Int = 0

    constructor(parcel: Parcel) : this() {
        assetReviewId = parcel.readLong()
        assetReviewDate = parcel.readString() ?: ""
        obs = parcel.readString() ?: ""
        userId = parcel.readLong()
        warehouseAreaId = parcel.readLong()
        warehouseId = parcel.readLong()
        modificationDate = parcel.readString() ?: ""
        collectorAssetReviewId = parcel.readLong()
        statusId = parcel.readInt()
    }

    fun getBySoapObject(so: SoapObject): AssetReviewObject {
        val x = AssetReviewObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "asset_review_id" -> {
                            x.assetReviewId = soValue as? Long ?: 0L
                        }
                        "asset_review_date" -> {
                            x.assetReviewDate = soValue as? String ?: ""
                        }
                        "obs" -> {
                            x.obs = soValue as? String ?: ""
                        }
                        "user_id" -> {
                            x.userId = soValue as? Long ?: 0L
                        }
                        "warehouse_area_id" -> {
                            x.warehouseAreaId = soValue as? Long ?: 0L
                        }
                        "warehouse_id" -> {
                            x.warehouseId = soValue as? Long ?: 0L
                        }
                        "modification_date" -> {
                            x.modificationDate = soValue as? String ?: ""
                        }
                        "collector_asset_review_id" -> {
                            x.collectorAssetReviewId = soValue as? Long ?: 0L
                        }
                        "status_id" -> {
                            x.statusId = soValue as? Int ?: 0
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(assetReviewId)
        parcel.writeString(assetReviewDate)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(warehouseAreaId)
        parcel.writeLong(warehouseId)
        parcel.writeString(modificationDate)
        parcel.writeLong(collectorAssetReviewId)
        parcel.writeInt(statusId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewObject> {
        override fun createFromParcel(parcel: Parcel): AssetReviewObject {
            return AssetReviewObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewObject?> {
            return arrayOfNulls(size)
        }
    }
}


