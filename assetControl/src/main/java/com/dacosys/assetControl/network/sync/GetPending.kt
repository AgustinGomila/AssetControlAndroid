package com.dacosys.assetControl.network.sync

import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.utils.Statics
import kotlinx.coroutines.*

class GetPending(
    private var onPendingProgress: (ArrayList<Any>) -> Unit = {},
) {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<ArrayList<Any>>? = null
    private suspend fun doInBackground() {
        var result: ArrayList<Any> = ArrayList()
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: ArrayList()
        }
        onPendingProgress.invoke(result)
    }

    private suspend fun suspendFunction(): ArrayList<Any> = withContext(Dispatchers.IO) {
        val syncElements: ArrayList<Any> = ArrayList()

        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getReadableDb()

        try {
            db.beginTransaction()

            val ar = Statics.pendingAssetReview()
            val wm = Statics.pendingWarehouseMovement()
            val a = Statics.pendingAsset()
            val wa = Statics.pendingWarehouseArea()
            val w = Statics.pendingWarehouse()
            val ic = Statics.pendingItemCategory()
            val dc = Statics.pendingDataCollection()
            val rp = Statics.pendingRouteProcess()
            val am = Statics.pendingAssetManteinance()
            val pendingImages = com.dacosys.imageControl.Statics.pendingImages()

            db.setTransactionSuccessful()

            if (ar.any()) syncElements.addAll(ar)
            if (wm.any()) syncElements.addAll(wm)
            if (a.any()) syncElements.addAll(a)
            if (wa.any()) syncElements.addAll(wa)
            if (w.any()) syncElements.addAll(w)
            if (ic.any()) syncElements.addAll(ic)
            if (dc.any()) syncElements.addAll(dc)
            if (rp.any()) syncElements.addAll(rp)
            if (am.any()) syncElements.addAll(am)
            if (pendingImages > 0) syncElements.addAll(arrayOf(pendingImages))
        } finally {
            db.endTransaction()
        }

        return@withContext syncElements
    }

    init {
        scope.launch { doInBackground() }
    }
}