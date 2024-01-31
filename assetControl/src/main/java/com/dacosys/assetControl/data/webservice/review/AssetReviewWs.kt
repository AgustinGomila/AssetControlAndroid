package com.dacosys.assetControl.data.webservice.review

import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AssetReviewWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun assetReviewGetAll(
    ): Array<AssetReviewObject>? {
        val any = getWebservice().s(
            "AssetReview_GetAll"
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetReviewObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetReviewObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetReviewGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetReviewObject>? {
        val any = getWebservice().s(
            "AssetReview_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetReviewObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetReviewObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetReviewGet(
        assetReviewId: Long,
    ): AssetReviewObject {
        val soapObject = getWebservice().s(
            "AssetReview_Get",
            arrayOf(
                WsParam("asset_review_id", assetReviewId)
            )
        ) as SoapObject
        return AssetReviewObject().getBySoapObject(soapObject)
    }

    @Throws(Exception::class)
    fun assetReviewModify(
        userId: Long,
        assetReview: AssetReviewObject,
        assetReviewContent: ArrayList<AssetReviewContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "asset_review")
        arSoapObject.addProperty("asset_review_id", assetReview.assetReviewId)
        arSoapObject.addProperty("asset_review_date", assetReview.assetReviewDate)
        arSoapObject.addProperty("obs", assetReview.obs)
        arSoapObject.addProperty("user_id", assetReview.userId)
        arSoapObject.addProperty("warehouse_id", assetReview.warehouseId)
        arSoapObject.addProperty("warehouse_area_id", assetReview.warehouseAreaId)
        arSoapObject.addProperty("status_id", assetReview.statusId)
        arSoapObject.addProperty("modification_date", assetReview.modificationDate)

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (assetReviewContent.size > 0) {
            assetReviewContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("asset_review_id", t.assetReviewId)
                arcSoapObject.addProperty("asset_review_content_id", t.assetReviewContentId)
                arcSoapObject.addProperty("asset_id", t.assetId)
                arcSoapObject.addProperty("code", t.code)
                arcSoapObject.addProperty("description", t.description)
                arcSoapObject.addProperty("qty", t.qty)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        val result = getWebservice().s(
            "AssetReview_Modify",
            arrayOf(WsParam("user_id", userId)),
            arSoapObject,
            arcArrayObject.toTypedArray()
        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    @Throws(Exception::class)
    fun assetReviewAdd(
        assetReview: AssetReviewObject,
        assetReviewContent: ArrayList<AssetReviewContentObject>,
    ): Long {
        val arSoapObject = SoapObject(getWebservice().namespace, "asset_review")
        arSoapObject.addProperty("asset_review_id", assetReview.assetReviewId)
        arSoapObject.addProperty("asset_review_date", assetReview.assetReviewDate)
        arSoapObject.addProperty("obs", assetReview.obs)
        arSoapObject.addProperty("user_id", assetReview.userId)
        arSoapObject.addProperty("warehouse_id", assetReview.warehouseId)
        arSoapObject.addProperty("warehouse_area_id", assetReview.warehouseAreaId)
        arSoapObject.addProperty("status_id", assetReview.statusId)
        arSoapObject.addProperty("modification_date", assetReview.modificationDate)

        var arcArrayObject: ArrayList<SoapObject> = addNullContent()
        if (assetReviewContent.size > 0) {
            assetReviewContent.forEach { t ->
                val arcSoapObject = SoapObject(getWebservice().namespace, "content")

                arcSoapObject.addProperty("asset_review_id", t.assetReviewId)
                arcSoapObject.addProperty("asset_review_content_id", t.assetReviewContentId)
                arcSoapObject.addProperty("asset_id", t.assetId)
                arcSoapObject.addProperty("code", t.code)
                arcSoapObject.addProperty("description", t.description)
                arcSoapObject.addProperty("qty", t.qty)

                arcArrayObject.add(arcSoapObject)
            }
        } else {
            arcArrayObject = addNullContent()
        }

        // Revisar en el futuro por qué en este caso únicamente
        // tengo que armar el objeto del contenido de manera
        // diferente que en movimiento (por ejemplo)

        val result = getWebservice().s(
            "AssetReview_Add",
            arSoapObject,
            arcArrayObject.toTypedArray()

        ) ?: return 0
        return (result as? Int)?.toLong() ?: (result as? Long ?: 0)
    }

    private fun addNullContent(): ArrayList<SoapObject> {
        val arcArrayObject: ArrayList<SoapObject> = ArrayList()
        val arcSoapObject = SoapObject(getWebservice().namespace, "content")

        arcSoapObject.addProperty("asset_review_id", null)
        arcSoapObject.addProperty("asset_review_content_id", null)
        arcSoapObject.addProperty("asset_id", -999)
        arcSoapObject.addProperty("code", null)
        arcSoapObject.addProperty("description", null)
        arcSoapObject.addProperty("qty", null)

        arcArrayObject.add(arcSoapObject)

        return arcArrayObject
    }

    @Throws(Exception::class)
    fun assetReviewCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "AssetReview_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}