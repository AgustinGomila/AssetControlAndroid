package com.example.assetControl.network.sync

import com.example.assetControl.data.room.repository.barcode.BarcodeLabelTargetRepository
import com.example.assetControl.data.room.repository.maintenance.MaintenanceStatusRepository
import com.example.assetControl.data.room.repository.review.AssetReviewStatusRepository
import com.example.assetControl.data.room.repository.route.RouteProcessStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
