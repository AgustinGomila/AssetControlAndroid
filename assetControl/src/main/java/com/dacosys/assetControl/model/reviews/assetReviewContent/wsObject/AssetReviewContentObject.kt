package com.dacosys.assetControl.model.reviews.assetReviewContent.wsObject

import android.os.Parcel
import android.os.Parcelable
import org.ksoap2.serialization.SoapObject

class AssetReviewContentObject() : Parcelable {
    var assetReviewId: Long = 0
    var assetReviewContentId: Long = 0
    var assetId: Long = 0
    var code: String = ""
    var description: String = ""
    var qty: Float = 0F
    var contentStatusId: Int = 0
    var originWarehouseAreaId: Long = 0

    constructor(parcel: Parcel) : this() {
        this.assetReviewId = parcel.readLong()
        this.assetReviewContentId = parcel.readLong()
        this.assetId = parcel.readLong()
        this.code = parcel.readString() ?: ""
        this.description = parcel.readString() ?: ""
        this.qty = parcel.readFloat()
        this.contentStatusId = parcel.readInt()
        this.originWarehouseAreaId = parcel.readLong()
    }

    fun getBySoapObject(so: SoapObject): AssetReviewContentObject {
        val x = AssetReviewContentObject()

        for (i in 0 until so.propertyCount) {
            if (so.getProperty(i) != null) {
                val soName = so.getPropertyInfo(i).name
                val soValue = so.getProperty(i)

                if (soValue != null)
                    when (soName) {
                        "asset_review_id" -> {
                            x.assetReviewId = soValue as? Long ?: 0L
                        }
                        "asset_review_content_id" -> {
                            x.assetReviewContentId = soValue as? Long ?: 0L
                        }
                        "asset_id" -> {
                            x.assetId = soValue as? Long ?: 0L
                        }
                        "code" -> {
                            x.code = soValue as? String ?: ""
                        }
                        "description" -> {
                            x.description = soValue as? String ?: ""
                        }
                        "qty" -> {
                            x.qty = soValue as? Float ?: 0F
                        }
                        "content_status_id" -> {
                            x.contentStatusId = soValue as? Int ?: 0
                        }
                        "origin_warehouse_area_id" -> {
                            x.originWarehouseAreaId = soValue as? Long ?: 0L
                        }
                    }
            }
        }
        return x
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(assetReviewId)
        parcel.writeLong(assetReviewContentId)
        parcel.writeLong(assetId)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeFloat(qty)
        parcel.writeInt(contentStatusId)
        parcel.writeLong(originWarehouseAreaId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetReviewContentObject> {
        override fun createFromParcel(parcel: Parcel): AssetReviewContentObject {
            return AssetReviewContentObject(parcel)
        }

        override fun newArray(size: Int): Array<AssetReviewContentObject?> {
            return arrayOfNulls(size)
        }
    }
}