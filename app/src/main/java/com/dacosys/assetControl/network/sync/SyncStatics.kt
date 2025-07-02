package com.dacosys.assetControl.network.sync

import com.dacosys.assetControl.data.room.repository.barcode.BarcodeLabelTargetRepository
import com.dacosys.assetControl.data.room.repository.maintenance.MaintenanceStatusRepository
import com.dacosys.assetControl.data.room.repository.review.AssetReviewStatusRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessStatusRepository
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
            async { AssetReviewStatusRepository().sync() },
            async { RouteProcessStatusRepository().sync() },
            async { MaintenanceStatusRepository().sync() },
            async { BarcodeLabelTargetRepository().sync() }
        )

        // De esta manera se mantiene la ejecución de los threads
        // dentro del bucle y sale recién cuando terminó con el último
        return@withContext t.awaitAll()
    }

    init {
        scope.launch { doInBackground() }
    }
}
