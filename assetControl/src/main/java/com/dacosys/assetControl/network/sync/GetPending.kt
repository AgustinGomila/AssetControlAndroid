package com.dacosys.assetControl.network.sync

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.utils.settings.preferences.Repository
import com.dacosys.imageControl.room.database.IcDatabase
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

        val ar = DownloadDb.getPendingAssetReview()
        val wm = DownloadDb.getPendingWarehouseMovement()
        val a = DownloadDb.getPendingAsset()
        val wa = DownloadDb.getPendingWarehouseArea()
        val w = DownloadDb.getPendingWarehouse()
        val ic = DownloadDb.getPendingItemCategory()
        val dc = DownloadDb.getPendingDataCollection()
        val rp = DownloadDb.getPendingRouteProcess()
        val am = DownloadDb.getPendingAssetMaintenance()

        if (ar.any()) syncElements.addAll(ar)
        if (wm.any()) syncElements.addAll(wm)
        if (a.any()) syncElements.addAll(a)
        if (wa.any()) syncElements.addAll(wa)
        if (w.any()) syncElements.addAll(w)
        if (ic.any()) syncElements.addAll(ic)
        if (dc.any()) syncElements.addAll(dc)
        if (rp.any()) syncElements.addAll(rp)
        if (am.any()) syncElements.addAll(am)

        if (Repository.useImageControl) {
            val pendingImages = IcDatabase.getDatabase(context = getContext()).imageDao().getPending()
            if (pendingImages.any()) syncElements.addAll(ArrayList(pendingImages))
        }

        return@withContext syncElements
    }

    init {
        scope.launch { doInBackground() }
    }
}