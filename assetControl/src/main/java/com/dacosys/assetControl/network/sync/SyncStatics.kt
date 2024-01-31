package com.dacosys.assetControl.network.sync

import com.dacosys.assetControl.data.dataBase.barcode.BarcodeLabelTargetDbHelper
import com.dacosys.assetControl.data.dataBase.manteinance.ManteinanceStatusDbHelper
import com.dacosys.assetControl.data.dataBase.review.AssetReviewStatusDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessStatusDbHelper
import kotlinx.coroutines.*

class SyncStatics {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private suspend fun doInBackground() {
        coroutineScope {
            launch { suspendFunction() }
        }
    }

    private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
        val t = listOf(
            async { AssetReviewStatusDbHelper().sync() },
            async { RouteProcessStatusDbHelper().sync() },
            async { ManteinanceStatusDbHelper().sync() },
            async { BarcodeLabelTargetDbHelper().sync() })

        // De esta manera se mantiene la ejecución de los threads
        // dentro del bucle y sale recién cuando terminó con el último
        return@withContext t.awaitAll()
    }

    init {
        scope.launch { doInBackground() }
    }
}
