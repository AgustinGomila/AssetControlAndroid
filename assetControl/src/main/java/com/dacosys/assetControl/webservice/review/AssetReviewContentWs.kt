package com.dacosys.assetControl.webservice.review

import com.dacosys.assetControl.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.webservice.common.WsParam
import org.ksoap2.serialization.SoapObject
import java.util.*

@Suppress("unused")
class AssetReviewContentWs @Throws(Exception::class)
constructor() {
    @Throws(Exception::class)
    fun assetReviewContentGet(
        assetReviewId: Long,
    ): Array<AssetReviewContentObject>? {
        val any = getWebservice().s(
            "AssetReviewContent_Get",
            arrayOf(
                WsParam("asset_review_id", assetReviewId)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetReviewContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetReviewContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetReviewContentGetAllLimit(
        pos: Int,
        qty: Int,
        date: String,
    ): Array<AssetReviewContentObject>? {
        val any = getWebservice().s(
            "AssetReviewContent_GetAll_Limit",
            arrayOf(
                WsParam("pos", pos),
                WsParam("qty", qty),
                WsParam("date", date)
            )
        )
        if (any != null) {
            val soVector = any as Vector<*>
            val dObjAl: ArrayList<AssetReviewContentObject> = ArrayList()
            for (soDc in soVector) {
                if (soDc !is SoapObject)
                    continue

                dObjAl.add(AssetReviewContentObject().getBySoapObject(soDc))
            }
            return dObjAl.toTypedArray()
        }
        return null
    }

    @Throws(Exception::class)
    fun assetReviewContentDelete(
        assetReviewId: Long,
    ): Boolean {
        return getWebservice().s(
            "AssetReviewContent_Delete_All",
            arrayOf(
                WsParam("asset_review_id", assetReviewId)
            )
        ) as Boolean
    }

    @Throws(Exception::class)
    fun assetReviewContentCount(date: String): Int? {
        val result = getWebservice().s(
            methodName = "AssetReviewContent_Count",
            params = arrayOf(WsParam("date", date))
        )
        return when (result) {
            is Int -> result
            else -> null
        }
    }
}