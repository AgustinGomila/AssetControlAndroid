package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.attribute.AttributeCompositionDbHelper
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentDbHelper
import com.dacosys.assetControl.dataBase.route.RouteProcessDbHelper
import com.dacosys.assetControl.model.route.Route
import com.dacosys.assetControl.model.route.RouteProcess
import kotlinx.coroutines.*

class GetRouteProcess(
    private var route: Route,
    private var onProgress: (RouteProcessResult) -> Unit = {},
) {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<RouteProcessResult>? = null
    private suspend fun doInBackground() {
        var result = RouteProcessResult()
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: RouteProcessResult()
        }
        onProgress.invoke(result)
    }

    private suspend fun suspendFunction(): RouteProcessResult = withContext(Dispatchers.IO) {
        val routeProcess: RouteProcess?

        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getWritableDb()

        val rpDbH = RouteProcessDbHelper()

        try {
            db.beginTransaction()

            // Comprobamos la integridad de las composiciones de todos los atributos de la ruta.
            val allAttrCompIdRoute =
                DataCollectionRuleContentDbHelper().selectAttributeCompositionIdByRouteId(route.routeId)
            val allAComp = AttributeCompositionDbHelper().select()

            val allAttrCompIdAvailable = ArrayList<Long>()
            for (aC in allAComp) {
                allAttrCompIdAvailable.add(aC.attributeCompositionId)
            }

            if (!allAttrCompIdAvailable.containsAll(allAttrCompIdRoute)) {

                db.setTransactionSuccessful()

                // LA RUTA ESTA INCOMPLETA
                return@withContext RouteProcessResult(
                    routeProcess = null,
                    newProcess = false,
                    error = ErrorResult(
                        AssetControlApp.getContext()
                            .getString(R.string.some_of_the_attributes_required_for_the_route_are_not_found_in_the_collector_database_and_require_synchronization)
                    )
                )
            }

            val rpArray = rpDbH.selectByRouteIdNoCompleted(route.routeId)

            // Si no hay procesos abiertos para esa ruta, abro uno nuevo
            // Si no utilizo el existente
            if (rpArray.size <= 0) {
                // NUEVO PROCESO DE RUTA
                val rpCollId = rpDbH.insert(route)
                routeProcess = rpDbH.selectById(rpCollId)

                db.setTransactionSuccessful()

                return@withContext RouteProcessResult(routeProcess, true)
            } else {
                // Comprobar que la composición de la ruta del proceso abierto
                // coincida con la composición de la ruta en la base de datos
                // ya que si se actualizó la ruta, el proceso es inválido
                routeProcess = rpArray[0]
                val rpcArray = routeProcess.contents
                val rcArray = route.composition

                db.setTransactionSuccessful()

                if (rcArray.size <= 0) {

                    // RUTA está vacía
                    return@withContext RouteProcessResult(
                        routeProcess = null,
                        newProcess = false,
                        error = ErrorResult(
                            AssetControlApp.getContext()
                                .getString(R.string.empty_route)
                        )
                    )
                }

                var error = false
                for (rpc in rpcArray) {
                    if (!rcArray.any { it.level == rpc.level && it.position == rpc.position }) {
                        error = true
                        break
                    }
                }

                if (!error) {
                    for (rc in rcArray) {
                        if (!rpcArray.any { it.level == rc.level && it.position == rc.position }) {
                            error = true
                            break
                        }
                    }
                }

                if (error) {
                    return@withContext RouteProcessResult(
                        routeProcess = null,
                        newProcess = false,
                        error = ErrorResult(
                            AssetControlApp.getContext()
                                .getString(R.string.the_composition_of_the_route_process_that_you_want_to_continue_is_different_from_the_current_composition_of_the_route_this_process_can_not_be_continued_and_must_be_canceled)
                        )
                    )
                }

                // CONTINUAR PROCESO DE RUTA
                return@withContext RouteProcessResult(
                    routeProcess = routeProcess,
                    newProcess = false
                )
            }
        } finally {
            db.endTransaction()
        }
    }

    init {
        scope.launch { doInBackground() }
    }
}   