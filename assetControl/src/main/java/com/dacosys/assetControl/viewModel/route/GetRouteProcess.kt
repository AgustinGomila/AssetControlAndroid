package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.entity.route.Route
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import com.dacosys.assetControl.data.room.repository.attribute.AttributeCompositionRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRuleContentRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessRepository
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
        val contentRepository = DataCollectionRuleContentRepository()
        val compositionRepository = AttributeCompositionRepository()

        ///////////////////////////////////
        // Para controlar la transacción //
        // TODO: Eliminar val db = DataBaseHelper.getWritableDb()

        val processRepository = RouteProcessRepository()

        try {
            // TODO: Eliminar db.beginTransaction()

            // Comprobamos la integridad de las composiciones de todos los atributos de la ruta.
            val allAttrCompIdRoute = contentRepository.selectAttributeCompositionIdByRouteId(route.id)
            val allAComp = compositionRepository.select()

            val allAttrCompIdAvailable = ArrayList<Long>()
            for (aC in allAComp) {
                allAttrCompIdAvailable.add(aC.id)
            }

            if (!allAttrCompIdAvailable.containsAll(allAttrCompIdRoute)) {

                // TODO: Eliminar db.setTransactionSuccessful()

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

            val rpArray = processRepository.selectByRouteIdNoCompleted(route.id)

            // Si no hay procesos abiertos para esa ruta, abro uno nuevo
            // Si no utilizo el existente
            if (rpArray.isEmpty()) {
                // NUEVO PROCESO DE RUTA
                val rpCollId = processRepository.insert(route)
                routeProcess = processRepository.selectById(rpCollId)

                // TODO: Eliminar db.setTransactionSuccessful()

                return@withContext RouteProcessResult(routeProcess, true)
            } else {
                // Comprobar que la composición de la ruta del proceso abierto
                // coincida con la composición de la ruta en la base de datos,
                // ya que si se actualizó la ruta, el proceso es inválido
                routeProcess = rpArray[0]
                val rpcArray = routeProcess.contents()
                val rcArray = route.composition()

                // TODO: Eliminar db.setTransactionSuccessful()

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
            // TODO: Eliminar db.endTransaction()
        }
    }

    init {
        scope.launch { doInBackground() }
    }
}   