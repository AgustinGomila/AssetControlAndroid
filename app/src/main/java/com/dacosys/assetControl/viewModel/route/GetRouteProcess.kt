package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dto.route.Route
import com.dacosys.assetControl.data.room.dto.route.RouteProcess
import com.dacosys.assetControl.data.room.repository.attribute.AttributeCompositionRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRuleContentRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessRepository
import kotlinx.coroutines.*

class GetRouteProcess(
    private var userId: Long,
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
        val processRepository = RouteProcessRepository()

        // Comprobamos la integridad de las composiciones de todos los atributos de la ruta.
        val allAttrCompIdRoute = contentRepository.selectAttributeCompositionIdByRouteId(route.id)
        val allAComp = compositionRepository.select()

        val allAttrCompIdAvailable = allAComp.map { it.id }

        if (!allAttrCompIdAvailable.containsAll(allAttrCompIdRoute)) {
            // LA RUTA ESTA INCOMPLETA
            return@withContext RouteProcessResult(
                routeProcess = null,
                newProcess = false,
                error = ErrorResult(
                    AssetControlApp.context
                        .getString(R.string.some_of_the_attributes_required_for_the_route_are_not_found_in_the_collector_database_and_require_synchronization)
                )
            )
        }

        val uncompletedProcess = processRepository.selectByRouteIdNoCompleted(route.id)

        // Si no hay procesos abiertos para esa ruta, creamos uno nuevo.
        // Si no, continuamos el proceso existente.

        if (uncompletedProcess.isEmpty()) {

            // Iniciar NUEVO PROCESO DE RUTA

            routeProcess = processRepository.insert(userId, route)

            return@withContext RouteProcessResult(
                routeProcess = routeProcess,
                newProcess = true
            )
        } else {

            // Comprobar que la composición de la ruta del proceso abierto
            // coincida con la composición de la ruta en la base de datos,
            // ya que si se actualizó la ruta, el proceso es inválido

            routeProcess = uncompletedProcess[0]
            val processContents = routeProcess.contents()
            val routeComposition = route.composition()

            if (routeComposition.isEmpty()) {

                // RUTA está vacía
                return@withContext RouteProcessResult(
                    routeProcess = null,
                    newProcess = false,
                    error = ErrorResult(
                        AssetControlApp.context
                            .getString(R.string.empty_route)
                    )
                )
            }

            var error = false
            for (rpc in processContents) {
                if (!routeComposition.any {
                        it.level == rpc.level && it.position == rpc.position
                    }) {
                    error = true
                    break
                }
            }

            if (!error) {
                for (rc in routeComposition) {
                    if (!processContents.any {
                            it.level == rc.level && it.position == rc.position
                        }) {
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
                        AssetControlApp.context
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
    }

    init {
        scope.launch { doInBackground() }
    }
}   