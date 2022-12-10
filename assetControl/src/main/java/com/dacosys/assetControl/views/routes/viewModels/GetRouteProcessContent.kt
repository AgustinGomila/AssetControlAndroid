package com.dacosys.assetControl.views.routes.viewModels

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import kotlinx.coroutines.*

data class GetRouteProcessContentResult(
    val currentRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList(),
    val level: Int = 0,
)

class GetRouteProcessContent(
    private val routeId: Long,
    private val routeProcessId: Long,
    private val level: Int,
    private var rpContArray: ArrayList<RouteProcessContent> = ArrayList(),
    private var onProgress: (GetRouteProcessContentResult) -> Unit = {},
) {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<GetRouteProcessContentResult>? = null
    private suspend fun doInBackground() {
        var result = GetRouteProcessContentResult()
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: GetRouteProcessContentResult()
        }
        onProgress.invoke(result)
    }

    private suspend fun suspendFunction(): GetRouteProcessContentResult =
        withContext(Dispatchers.IO) {

            // Me quedo con el contenido del nivel que estamos registrando
            var thisLevelRpCont: ArrayList<RouteProcessContent> = ArrayList()

            ///////////////////////////////////
            // Para controlar la transacción //
            val db = DataBaseHelper.getWritableDb()

            try {
                db.beginTransaction()

                if (rpContArray.size < 1) {
                    Log.d(this::class.java.simpleName,
                        getContext().getString(R.string.getting_processed_content))

                    // Necesita llamarse al GetByRouteProcessComplete para que se inserten
                    // los registros aún no recolectados de la ruta
                    val tempRpCont =
                        RouteProcessContentDbHelper().selectByRouteProcessComplete(routeId,
                            routeProcessId,
                            null)

                    for (rpCont in tempRpCont) {
                        rpContArray.add(rpCont)

                        if (rpCont.routeProcessId == routeProcessId && rpCont.level == level) {
                            thisLevelRpCont.add(rpCont)
                        }
                    }

                    thisLevelRpCont =
                        ArrayList(thisLevelRpCont.sortedWith(compareBy({ it.level },
                            { it.position })))
                } else {
                    for (rpCont in rpContArray) {
                        if (rpCont.routeProcessId == routeProcessId && rpCont.level == level) {
                            thisLevelRpCont.add(rpCont)
                        }
                    }

                    thisLevelRpCont =
                        ArrayList(thisLevelRpCont.sortedWith(compareBy({ it.level },
                            { it.position })))
                }

                db.setTransactionSuccessful()

            } finally {
                db.endTransaction()
            }

            return@withContext GetRouteProcessContentResult(currentRouteProcessContent = thisLevelRpCont,
                level = level)
        }

    init {
        scope.launch { doInBackground() }
    }
}   