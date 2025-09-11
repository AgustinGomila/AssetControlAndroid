package com.example.assetControl.viewModel.route

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.data.room.repository.route.RouteProcessContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetRouteProcessContent(
    private val routeId: Long,
    private val routeProcessId: Long,
    private val level: Int,
    private var rpContArray: ArrayList<RouteProcessContent> = ArrayList(),
    private var onProgress: (RouteProcessContentResult) -> Unit = {},
) {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<RouteProcessContentResult>? = null
    private suspend fun doInBackground() {
        var result = RouteProcessContentResult()
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: RouteProcessContentResult()
        }
        onProgress.invoke(result)
    }

    private suspend fun suspendFunction(): RouteProcessContentResult = withContext(Dispatchers.IO) {
        // Me quedo con el contenido del nivel que estamos registrando
        var thisLevelRpCont: ArrayList<RouteProcessContent> = ArrayList()

        if (rpContArray.isEmpty()) {
            Log.d(
                this::class.java.simpleName,
                context.getString(R.string.getting_processed_content)
            )

            // Necesita llamarse al [selectByRouteProcessComplete] para que se inserten
            // los registros aún no recolectados de la ruta
            val tempRpCont =
                RouteProcessContentRepository().selectByRouteProcessComplete(
                    routeId = routeId,
                    routeProcessId = routeProcessId,
                    dataCollection = null
                )

            for (rpCont in tempRpCont) {
                rpContArray.add(rpCont)

                if (rpCont.routeProcessId == routeProcessId && rpCont.level == level) {
                    thisLevelRpCont.add(rpCont)
                }
            }

            thisLevelRpCont =
                ArrayList(
                    thisLevelRpCont.sortedWith(
                        compareBy(
                            { it.level },
                            { it.position })
                    )
                )
        } else {
            for (rpCont in rpContArray) {
                if (rpCont.routeProcessId == routeProcessId && rpCont.level == level) {
                    thisLevelRpCont.add(rpCont)
                }
            }

            thisLevelRpCont =
                ArrayList(
                    thisLevelRpCont.sortedWith(
                        compareBy(
                            { it.level },
                            { it.position })
                    )
                )
        }

        return@withContext RouteProcessContentResult(
            contents = thisLevelRpCont,
            level = level
        )
    }

    init {
        scope.launch { doInBackground() }
    }
}   