package com.example.assetControl.network.sync

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.getUserId
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.attribute.AttributeComposition
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleTarget
import com.example.assetControl.data.room.dto.maintenance.MaintenanceType
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeGroup
import com.example.assetControl.data.room.dto.route.RouteComposition
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.attribute.AttributeCategoryRepository
import com.example.assetControl.data.room.repository.attribute.AttributeCompositionRepository
import com.example.assetControl.data.room.repository.attribute.AttributeRepository
import com.example.assetControl.data.room.repository.barcode.BarcodeLabelCustomRepository
import com.example.assetControl.data.room.repository.category.ItemCategoryRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRuleContentRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRuleRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRuleTargetRepository
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.room.repository.location.WarehouseRepository
import com.example.assetControl.data.room.repository.maintenance.MaintenanceTypeGroupRepository
import com.example.assetControl.data.room.repository.maintenance.MaintenanceTypeRepository
import com.example.assetControl.data.room.repository.route.RouteCompositionRepository
import com.example.assetControl.data.room.repository.route.RouteRepository
import com.example.assetControl.data.room.repository.user.UserPermissionRepository
import com.example.assetControl.data.room.repository.user.UserWarehouseAreaRepository
import com.example.assetControl.data.webservice.asset.AssetWs
import com.example.assetControl.data.webservice.attribute.AttributeCategoryWs
import com.example.assetControl.data.webservice.attribute.AttributeCompositionWs
import com.example.assetControl.data.webservice.attribute.AttributeObject
import com.example.assetControl.data.webservice.attribute.AttributeWs
import com.example.assetControl.data.webservice.barcode.BarcodeLabelCustomWs
import com.example.assetControl.data.webservice.category.ItemCategoryWs
import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.data.webservice.dataCollection.DataCollectionRuleContentWs
import com.example.assetControl.data.webservice.dataCollection.DataCollectionRuleTargetWs
import com.example.assetControl.data.webservice.dataCollection.DataCollectionRuleWs
import com.example.assetControl.data.webservice.location.WarehouseAreaWs
import com.example.assetControl.data.webservice.location.WarehouseWs
import com.example.assetControl.data.webservice.maintenance.MaintenanceTypeWs
import com.example.assetControl.data.webservice.maintenance.ManteinanceTypeGroupWs
import com.example.assetControl.data.webservice.route.RouteCompositionWs
import com.example.assetControl.data.webservice.route.RouteWs
import com.example.assetControl.data.webservice.user.UserPermissionWs
import com.example.assetControl.data.webservice.user.UserWarehouseAreaWs
import com.example.assetControl.network.serverDate.GetMySqlDate
import com.example.assetControl.network.serverDate.MySqlDateResult
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.network.utils.SetCurrentSession
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.Statics.Companion.TIME_FILE_NAME
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.entries.ConfEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class SyncDownload(
    private var onSyncTaskProgress: (SyncProgress) -> Unit = {},
    private var onSessionCreated: (Boolean) -> Unit = {},
) {
    private var registryOnProcess: ArrayList<SyncRegistryType> = ArrayList()
    private var registryTypeUpdated = ArrayList<SyncRegistryType>()
    private var serverTime: String = ""

    private fun onConnectionResult(r: MySqlDateResult) {
        if (r.status == ProgressStatus.finished) {
            this.serverTime = r.msg
            launchDownload()
        } else if (r.status == ProgressStatus.crashed || r.status == ProgressStatus.canceled) {
            scope.launch {
                onUiEvent(
                    SyncProgress(
                        msg = serverTime,
                        progressStatus = r.status
                    )
                )
            }
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private fun launchDownload() {
        fun onFinish(it: Boolean) {
            val sp: SyncProgress = if (it) {
                SyncProgress(
                    msg = context.getString(R.string.download_finished),
                    progressStatus = ProgressStatus.bigFinished
                )
            } else {
                SyncProgress(
                    msg = context.getString(R.string.error_downloading_the_database_from_the_server),
                    progressStatus = ProgressStatus.crashed
                )
            }

            scope.launch {
                onUiEvent(sp)
            }
        }

        scope.launch {
            doInBackground(onFinish = { onFinish(it) })
        }
    }

    private suspend fun onSessionEvent(it: Boolean) {
        withContext(Dispatchers.Main) {
            onSessionCreated.invoke(it)
        }
    }

    private suspend fun onUiEvent(it: SyncProgress) {
        withContext(Dispatchers.Main) {
            onSyncTaskProgress.invoke(it)
        }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(onFinish: (Boolean) -> Unit) {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() == true
        }
        onFinish.invoke(result)
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.downloading_starting),
                progressStatus = ProgressStatus.bigStarting
            )
        )

        qty = sr.prefsGetInt(ConfEntry.acSyncQtyRegistry)

        if (Statics.currentSession == null) {
            SetCurrentSession(onSessionCreated = { scope.launch { onSessionEvent(it) } })
        }

        try {
            val t = listOf(
                async { user() },
                async { asset() },
                async { itemCategory() },
                async { warehouse() },
                async { warehouseArea() },
                async { attribute() },
                async { attributeCategory() },
                async { route() },
                async { dataCollectionRule() },
                async { barcodeLabelCustom() })

            if (sr.prefsGetBoolean(Preference.useAssetControlManteinance)) {
                t.union(
                    listOf(
                        async { maintenanceType() },
                        async { maintenanceTypeGroup() })
                )
            }

            // De esta manera se mantiene la ejecución de los threads
            // dentro del bucle y sale recién cuando terminó con el último

            t.awaitAll()

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_failed),
                    progressStatus = ProgressStatus.bigCrashed
                )
            )
            return@withContext false
        } finally {
            saveLastUpdateDates()
        }

        return@withContext true
    }

    private fun saveLastUpdateDates(): Boolean {
        try {
            if (serverTime.isNotEmpty() && scope.isActive) {
                val registries: ArrayList<String> = ArrayList()
                for (registryType in registryTypeUpdated) {
                    if (registryType.confEntry == null) continue
                    Log.d(
                        this::class.java.simpleName, "${
                            context.getString(R.string.saving_synchronization_time)
                        }: ${registryType.confEntry!!.description} (${serverTime})"
                    )
                    registries.add(registryType.confEntry!!.description)
                }
                sr.prefsPutString(registries, serverTime)
            }
            registryTypeUpdated.clear()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        }
        return true
    }

    private suspend fun asset() {
        val registryType = SyncRegistryType.Asset
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_asset_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = AssetWs()
        val assetRepository = AssetRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var currentCount = 0
        var groupCount = 0
        var pos = 0
        try {
            countTotal = ws.assetCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.assetGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        assetRepository.sync(
                            assetsObj = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_asset_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_asset_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.asset_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun itemCategory() {
        val registryType = SyncRegistryType.ItemCategory
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_category_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = ItemCategoryWs()
        val categoryRepository = ItemCategoryRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.itemCategoryCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.itemCategoryGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        categoryRepository.sync(
                            icObj = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_category_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_category_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.category_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    @Suppress("unused")
    private suspend fun user() {
        val registryType = SyncRegistryType.User
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_user_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val upWs = UserPermissionWs()
        val uwaWs = UserWarehouseAreaWs()

        val permissionRepository = UserPermissionRepository()
        val userAreaRepository = UserWarehouseAreaRepository()

        // Eliminar datos antiguos de los usuarios
        permissionRepository.deleteAll()
        userAreaRepository.deleteAll()

        try {
            if (!scope.isActive) return
            var currentUserId = getUserId() ?: return

            try {
                // Sincronizar los permisos del usuario
                var perm = upWs.userPermissionGet(currentUserId) ?: arrayOf()
                permissionRepository.insert(perm.toList()) { scope.launch { onUiEvent(it) } }

                // Sincronizar las áreas del usuario
                var areas = uwaWs.userWarehouseAreaGet(currentUserId) ?: arrayOf()
                userAreaRepository.insert(areas.toList()) { scope.launch { onUiEvent(it) } }
            } catch (ex: Exception) {
                ex.printStackTrace()
                // Error local
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.local_error_in_user_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.crashed
                    )
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_user_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.user_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                registryTypeUpdated.add(registryType)
            }
        }
    }

    private suspend fun warehouse() {
        val registryType = SyncRegistryType.Warehouse
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_warehouse_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseWs()
        val warehouseRepository = WarehouseRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.warehouseCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.warehouseGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        warehouseRepository.sync(
                            wObj = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_warehouse_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_warehouse_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.warehouse_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun warehouseArea() {
        val registryType = SyncRegistryType.WarehouseArea
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_warehouse_area_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseAreaWs()
        val areaRepository = WarehouseAreaRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.warehouseAreaCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.warehouseAreaGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        areaRepository.sync(
                            areaObjects = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_warehouse_area_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_warehouse_area_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.warehouse_area_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun attribute() {
        val registryType = SyncRegistryType.Attribute
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_attribute_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = AttributeWs()
        val upWs = AttributeCompositionWs()

        val attributeRepository = AttributeRepository()
        val compositionRepository = AttributeCompositionRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.attributeCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.attributeGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        attributeRepository.sync(
                            attributeObjects = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                        attributeComposition(
                            ws = upWs, compositionRepository = compositionRepository, attr = objArray
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_attribute_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_attribute_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.attribute_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun attributeComposition(
        ws: AttributeCompositionWs,
        compositionRepository: AttributeCompositionRepository,
        attr: Array<AttributeObject>,
    ) {
        try {
            val ids = ArrayList<Long>()
            attr.mapTo(ids) { it.attributeId }
            compositionRepository.deleteByIds(ids)

            val allAttrComp: ArrayList<AttributeComposition> = ArrayList()
            var currentCount = 0
            for (a in attr) {
                currentCount++
                onUiEvent(
                    SyncProgress(
                        totalTask = attr.size,
                        completedTask = currentCount,
                        msg = context.getString(R.string.synchronizing_attribute_compositions),
                        registryType = SyncRegistryType.AttributeComposition,
                        progressStatus = ProgressStatus.running
                    )
                )

                val attributeId = a.attributeId
                val objArray = ws.attributeCompositionGet(attributeId)

                if (!objArray.isNullOrEmpty()) {
                    objArray.mapTo(allAttrComp) {
                        AttributeComposition(
                            id = it.attributeCompositionId,
                            attributeId = it.attributeId,
                            attributeCompositionTypeId = it.attributeCompositionTypeId,
                            description = it.description,
                            composition = it.composition,
                            used = it.used,
                            name = it.name,
                            readOnly = it.readOnly,
                            defaultValue = it.defaultValue
                        )
                    }
                }
            }
            compositionRepository.insert(allAttrComp)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private suspend fun attributeCategory() {
        val registryType = SyncRegistryType.AttributeCategory
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_attribute_category_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )
        registryOnProcess.add(registryType)
        val ws = AttributeCategoryWs()
        val categoryRepository = AttributeCategoryRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.attributeCategoryCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.attributeCategoryGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        categoryRepository.sync(
                            categoryObjects = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_attribute_category_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_attribute_category_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.attribute_category_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun route() {
        val registryType = SyncRegistryType.Route
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_route_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = RouteWs()
        val upWs = RouteCompositionWs()

        val routeRepository = RouteRepository()
        val compositionRepository = RouteCompositionRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.routeCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.routeGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        routeRepository.sync(
                            routeObjects = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size

                        for (obj in objArray) {
                            // route composition
                            routeComposition(
                                ws = upWs, compositionRepository = compositionRepository, routeId = obj.route_id
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_route_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_route_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.route_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun routeComposition(
        ws: RouteCompositionWs,
        compositionRepository: RouteCompositionRepository,
        routeId: Long,
    ) {
        try {
            val objArray = ws.routeCompositionGet(routeId)
            compositionRepository.deleteByRouteId(routeId)

            if (!objArray.isNullOrEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val rc: ArrayList<RouteComposition> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        onUiEvent(
                            SyncProgress(
                                totalTask = countTotal,
                                completedTask = currentCount,
                                msg = context.getString(R.string.synchronizing_route_contents),
                                registryType = SyncRegistryType.RouteComposition,
                                progressStatus = ProgressStatus.running
                            )
                        )

                        val routeComposition = RouteComposition(
                            routeId = obj.routeId,
                            dataCollectionRuleId = obj.dataCollectionRuleId,
                            level = obj.level,
                            position = obj.position,
                            assetId = obj.assetId,
                            warehouseId = obj.warehouseId,
                            warehouseAreaId = obj.warehouseAreaId,
                            expression = obj.expression,
                            trueResult = obj.trueResult,
                            falseResult = obj.falseResult
                        )
                        rc.add(routeComposition)
                    }
                    compositionRepository.insert(rc)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    // Error local
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    return
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private suspend fun dataCollectionRule() {
        val registryType = SyncRegistryType.DataCollectionRule
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_data_collection_rule_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = DataCollectionRuleWs()
        val upWs = DataCollectionRuleContentWs()
        val uwaWs = DataCollectionRuleTargetWs()

        val collectionRuleRepository = DataCollectionRuleRepository()
        val ruleContentRepository = DataCollectionRuleContentRepository()
        val ruleTargetRepository = DataCollectionRuleTargetRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.dataCollectionRuleCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.dataCollectionRuleGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        collectionRuleRepository.sync(
                            routeObjects = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size

                        for (obj in objArray) {
                            // dataCollectionRule content
                            dataCollectionRuleContent(
                                ws = upWs,
                                ruleContentRepository = ruleContentRepository,
                                dataCollectionRuleId = obj.dataCollectionRuleId
                            )

                            // dataCollectionRule warehouse area
                            dataCollectionRuleTarget(
                                ws = uwaWs,
                                ruleTargetRepository = ruleTargetRepository,
                                dataCollectionRuleId = obj.dataCollectionRuleId
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_data_collection_rule_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_data_collection_rule_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.data_collection_rule_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun dataCollectionRuleContent(
        ws: DataCollectionRuleContentWs,
        ruleContentRepository: DataCollectionRuleContentRepository,
        dataCollectionRuleId: Long,
    ) {
        try {
            val objArray = ws.dataCollectionRuleContentGet(dataCollectionRuleId)
            ruleContentRepository.deleteByDataCollectionRuleId(dataCollectionRuleId)

            if (!objArray.isNullOrEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val contents: ArrayList<DataCollectionRuleContent> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        onUiEvent(
                            SyncProgress(
                                totalTask = countTotal,
                                completedTask = currentCount,
                                msg = context.getString(R.string.synchronizing_data_collection_rule_contents),
                                registryType = SyncRegistryType.DataCollectionRuleContent,
                                progressStatus = ProgressStatus.running
                            )
                        )

                        val dataCollectionRuleContent = DataCollectionRuleContent(
                            id = obj.dataCollectionRuleContentId,
                            description = obj.description,
                            dataCollectionRuleId = obj.dataCollectionRuleId,
                            level = obj.level,
                            position = obj.position,
                            attributeId = obj.attributeId,
                            attributeCompositionId = obj.attributeCompositionId,
                            expression = obj.expression,
                            trueResult = obj.trueResult,
                            falseResult = obj.falseResult,
                            mActive = obj.active,
                            mMandatory = obj.mandatory
                        )
                        contents.add(dataCollectionRuleContent)
                    }
                    ruleContentRepository.insert(contents)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    // Error local
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    return
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private suspend fun dataCollectionRuleTarget(
        ws: DataCollectionRuleTargetWs,
        ruleTargetRepository: DataCollectionRuleTargetRepository,
        dataCollectionRuleId: Long,
    ) {
        try {
            val objArray = ws.dataCollectionRuleTargetGet(dataCollectionRuleId)
            ruleTargetRepository.deleteByDataCollectionRuleId(dataCollectionRuleId)

            if (!objArray.isNullOrEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val targets: ArrayList<DataCollectionRuleTarget> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        onUiEvent(
                            SyncProgress(
                                totalTask = countTotal,
                                completedTask = currentCount,
                                msg = context.getString(R.string.synchronizing_data_collection_rule_targets),
                                registryType = SyncRegistryType.DataCollectionRuleTarget,
                                progressStatus = ProgressStatus.running
                            )
                        )

                        val dataCollectionRuleTarget = DataCollectionRuleTarget(
                            dataCollectionRuleId = obj.dataCollectionRuleId,
                            assetId = obj.assetId,
                            warehouseId = obj.warehouseId,
                            warehouseAreaId = obj.warehouseAreaId,
                            itemCategoryId = obj.itemCategoryId
                        )
                        targets.add(dataCollectionRuleTarget)
                    }
                    ruleTargetRepository.insert(targets)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    // Error local
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    return
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private suspend fun maintenanceType() {
        val registryType = SyncRegistryType.MaintenanceType
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_maintenance_type_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = MaintenanceTypeWs()
        val typeRepository = MaintenanceTypeRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.maintenanceTypeCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.maintenanceTypeGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        for (obj in objArray) {
                            currentCount++
                            onUiEvent(
                                SyncProgress(
                                    totalTask = countTotal,
                                    completedTask = currentCount,
                                    msg = context.getString(R.string.synchronizing_maintenance_types),
                                    registryType = registryType,
                                    progressStatus = ProgressStatus.running
                                )
                            )

                            if (!scope.isActive) {
                                onUiEvent(
                                    SyncProgress(
                                        totalTask = 0,
                                        completedTask = 0,
                                        msg = context.getString(R.string.canceling_maintenance_type_synchronization),
                                        registryType = registryType,
                                        progressStatus = ProgressStatus.canceled
                                    )
                                )
                                return
                            }

                            val maintenanceType = MaintenanceType(
                                id = obj.manteinance_type_id,
                                description = obj.description,
                                active = obj.active,
                                maintenanceTypeGroupId = obj.manteinance_type_group_id
                            )
                            typeRepository.sync(maintenanceType)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_maintenance_type_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_maintenance_type_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.maintenance_type_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun maintenanceTypeGroup() {
        val registryType = SyncRegistryType.MaintenanceTypeGroup
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_maintenance_type_group_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = ManteinanceTypeGroupWs()
        val groupRepository = MaintenanceTypeGroupRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.manteinanceTypeGroupCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.manteinanceTypeGroupGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        for (obj in objArray) {
                            currentCount++
                            onUiEvent(
                                SyncProgress(
                                    totalTask = countTotal,
                                    completedTask = currentCount,
                                    msg = context.getString(R.string.synchronizing_maintenance_type_groups),
                                    registryType = registryType,
                                    progressStatus = ProgressStatus.starting
                                )
                            )

                            if (!scope.isActive) {
                                onUiEvent(
                                    SyncProgress(
                                        totalTask = 0,
                                        completedTask = 0,
                                        msg = context.getString(R.string.canceling_maintenance_type_group_synchronization),
                                        registryType = registryType,
                                        progressStatus = ProgressStatus.canceled
                                    )
                                )
                                return
                            }

                            val typeGroup = MaintenanceTypeGroup(
                                id = obj.maintenanceTypeGroupId,
                                description = obj.description,
                                active = obj.active
                            )
                            groupRepository.sync(typeGroup)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_maintenance_type_group_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_maintenance_type_group_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.maintenance_type_group_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private suspend fun barcodeLabelCustom() {
        val registryType = SyncRegistryType.BarcodeLabelCustom
        onUiEvent(
            SyncProgress(
                msg = context.getString(R.string.starting_barcode_label_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = BarcodeLabelCustomWs()
        val barcodeRepository = BarcodeLabelCustomRepository()

        val date = sr.prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.barcodeLabelCustomCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.barcodeLabelCustomGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        barcodeRepository.sync(
                            assetsObj = objArray,
                            onSyncProgress = { scope.launch { onUiEvent(it) } },
                            count = currentCount,
                            total = countTotal
                        )
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = context.getString(R.string.local_error_in_barcode_label_synchronization),
                                registryType = registryType,
                                progressStatus = ProgressStatus.crashed
                            )
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.remote_error_in_barcode_label_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            !scope.isActive -> onUiEvent(
                SyncProgress(
                    msg = context.getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = context.getString(R.string.barcode_label_synchronization_completed),
                        registryType = registryType,
                        progressStatus = ProgressStatus.success
                    )
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    companion object {
        private var qty = 20

        fun resetSyncDates(): Boolean {
            val dbTime = getDateTimeStr()
            return resetSyncDates(dbTime)
        }

        private fun resetSyncDates(dbTime: String): Boolean {
            try {
                val registries: ArrayList<String> = ArrayList()
                for (registryType in SyncRegistryType.getSyncDownload()) {
                    registries.add(registryType.confEntry!!.description)
                }
                sr.prefsPutString(registries, dbTime)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return false
            }

            return true
        }

        private fun getDateTimeStr(): String {
            var dateTime = ""
            val timeFileLocation = File(context.cacheDir.absolutePath + "/" + TIME_FILE_NAME)

            //Read text from file
            try {
                val br = BufferedReader(FileReader(timeFileLocation.absolutePath))
                while (true) {
                    dateTime = br.readLine() ?: break
                }
                br.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e(
                    this::class.java.simpleName, "${
                        context.getString(R.string.failed_to_get_the_date_from_the_file)
                    }: ${ex.message}"
                )
            }
            return dateTime
        }
    }

    init {
        GetMySqlDate(getWebservice()) { onConnectionResult(it) }.execute()
    }
}