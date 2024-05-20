package com.dacosys.assetControl.network.sync

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionRuleTarget
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroup
import com.dacosys.assetControl.data.room.entity.route.RouteComposition
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.attribute.AttributeCategoryRepository
import com.dacosys.assetControl.data.room.repository.attribute.AttributeCompositionRepository
import com.dacosys.assetControl.data.room.repository.attribute.AttributeRepository
import com.dacosys.assetControl.data.room.repository.barcode.BarcodeLabelCustomRepository
import com.dacosys.assetControl.data.room.repository.category.ItemCategoryRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRuleContentRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRuleRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRuleTargetRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseRepository
import com.dacosys.assetControl.data.room.repository.maintenance.MaintenanceTypeGroupRepository
import com.dacosys.assetControl.data.room.repository.maintenance.MaintenanceTypeRepository
import com.dacosys.assetControl.data.room.repository.route.RouteCompositionRepository
import com.dacosys.assetControl.data.room.repository.route.RouteRepository
import com.dacosys.assetControl.data.room.repository.user.UserPermissionRepository
import com.dacosys.assetControl.data.room.repository.user.UserRepository
import com.dacosys.assetControl.data.room.repository.user.UserWarehouseAreaRepository
import com.dacosys.assetControl.data.webservice.asset.AssetWs
import com.dacosys.assetControl.data.webservice.attribute.AttributeCategoryWs
import com.dacosys.assetControl.data.webservice.attribute.AttributeCompositionWs
import com.dacosys.assetControl.data.webservice.attribute.AttributeObject
import com.dacosys.assetControl.data.webservice.attribute.AttributeWs
import com.dacosys.assetControl.data.webservice.barcode.BarcodeLabelCustomWs
import com.dacosys.assetControl.data.webservice.category.ItemCategoryWs
import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionRuleContentWs
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionRuleTargetWs
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionRuleWs
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaWs
import com.dacosys.assetControl.data.webservice.location.WarehouseWs
import com.dacosys.assetControl.data.webservice.maintenance.MaintenanceTypeWs
import com.dacosys.assetControl.data.webservice.maintenance.ManteinanceTypeGroupWs
import com.dacosys.assetControl.data.webservice.route.RouteCompositionWs
import com.dacosys.assetControl.data.webservice.route.RouteWs
import com.dacosys.assetControl.data.webservice.user.UserPermissionWs
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaWs
import com.dacosys.assetControl.data.webservice.user.UserWs
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.network.utils.SetCurrentSession
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.TIME_FILE_NAME
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutString
import kotlinx.coroutines.*
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
                    msg = getContext().getString(R.string.download_finished),
                    progressStatus = ProgressStatus.bigFinished
                )
            } else {
                SyncProgress(
                    msg = getContext().getString(R.string.error_downloading_the_database_from_the_server),
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
            result = deferred?.await() ?: false
        }
        onFinish.invoke(result)
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        onUiEvent(
            SyncProgress(
                msg = getContext().getString(R.string.downloading_starting),
                progressStatus = ProgressStatus.bigStarting
            )
        )

        qty = prefsGetInt(ConfEntry.acSyncQtyRegistry)

        if (Statics.currentSession == null) {
            SetCurrentSession(onSessionCreated = { scope.launch { onSessionEvent(it) } })
        }

        try {
            val t = listOf(
                async { asset() },
                async { itemCategory() },
                async { warehouse() },
                async { warehouseArea() },
                async { attribute() },
                async { attributeCategory() },
                async { route() },
                async { dataCollectionRule() },
                async { barcodeLabelCustom() })

            if (prefsGetBoolean(Preference.useAssetControlManteinance)) {
                t.union(
                    listOf(async { maintenanceType() }, async { maintenanceTypeGroup() })
                )
            }

            // De esta manera se mantiene la ejecución de los threads
            // dentro del bucle y sale recién cuando terminó con el último

            t.awaitAll()

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            onUiEvent(
                SyncProgress(
                    msg = getContext().getString(R.string.synchronization_failed),
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
                            getContext().getString(R.string.saving_synchronization_time)
                        }: ${registryType.confEntry!!.description} (${serverTime})"
                    )
                    registries.add(registryType.confEntry!!.description)
                }
                prefsPutString(registries, serverTime)
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
                msg = getContext().getString(R.string.starting_asset_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = AssetWs()
        val assetRepository = AssetRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_asset_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_asset_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.asset_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_category_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = ItemCategoryWs()
        val categoryRepository = ItemCategoryRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_category_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_category_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.category_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_user_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = UserWs()
        val upWs = UserPermissionWs()
        val uwaWs = UserWarehouseAreaWs()

        val userRepository = UserRepository()
        val permissionRepository = UserPermissionRepository()
        val userAreaRepository = UserWarehouseAreaRepository()

        // Eliminar datos antiguos de los usuarios
        userRepository.deleteAll()
        permissionRepository.deleteAll()
        userAreaRepository.deleteAll()

        val date = prefsGetString(
            registryType.confEntry ?: return
        )

        val countTotal: Int?
        var errorOccurred = false
        var currentCount = 0
        var groupCount = 0
        var pos = 0
        try {
            countTotal = ws.userCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    if (!scope.isActive) break

                    val objArray = ws.userGetAllLimit(pos, qty, date)
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    try {
                        if (objArray.isNotEmpty()) {
                            userRepository.sync(
                                assetsObj = objArray,
                                onSyncProgress = { scope.launch { onUiEvent(it) } },
                                count = currentCount,
                                total = countTotal
                            )

                            currentCount += objArray.size

                            val total = objArray.count()
                            for ((index, obj) in objArray.withIndex()) {
                                // user permission
                                SyncInitialUser { scope.launch { onUiEvent(it) } }.userPermission(
                                    objArray = upWs.userPermissionGet(obj.user_id)
                                )

                                // user warehouse area
                                SyncInitialUser { scope.launch { onUiEvent(it) } }.userWarehouseArea(
                                    objArray = uwaWs.userWarehouseAreaGet(obj.user_id)
                                )

                                scope.launch {
                                    onUiEvent(
                                        SyncProgress(
                                            totalTask = total,
                                            completedTask = index + 1,
                                            msg = getContext().getString(R.string.synchronizing_users),
                                            registryType = SyncRegistryType.User,
                                            progressStatus = ProgressStatus.running
                                        )
                                    )
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = getContext().getString(R.string.local_error_in_user_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_user_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.user_synchronization_completed),
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

    private suspend fun warehouse() {
        val registryType = SyncRegistryType.Warehouse
        onUiEvent(
            SyncProgress(
                msg = getContext().getString(R.string.starting_warehouse_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseWs()
        val warehouseRepository = WarehouseRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_warehouse_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_warehouse_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.warehouse_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_warehouse_area_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseAreaWs()
        val areaRepository = WarehouseAreaRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_warehouse_area_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_warehouse_area_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.warehouse_area_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_attribute_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = AttributeWs()
        val upWs = AttributeCompositionWs()

        val attributeRepository = AttributeRepository()
        val compositionRepository = AttributeCompositionRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_attribute_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_attribute_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.attribute_synchronization_completed),
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
                        msg = getContext().getString(R.string.synchronizing_attribute_compositions),
                        registryType = SyncRegistryType.AttributeComposition,
                        progressStatus = ProgressStatus.running
                    )
                )

                val attributeId = a.attributeId
                val objArray = ws.attributeCompositionGet(attributeId)
                if (!objArray.isNullOrEmpty()) {
                    for (obj in objArray) {
                        val attributeComposition = AttributeComposition(
                            id = obj.attributeCompositionId,
                            attributeId = obj.attributeId,
                            attributeCompositionTypeId = obj.attributeCompositionTypeId,
                            description = obj.description,
                            composition = obj.composition,
                            used = obj.used,
                            name = obj.name,
                            readOnly = obj.readOnly,
                            defaultValue = obj.defaultValue
                        )

                        Log.d(
                            this::class.java.simpleName,
                            "SQLITE-QUERY-INSERT-->" + attributeId + "," + obj.attributeCompositionId
                        )
                        allAttrComp.add(attributeComposition)
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
                msg = getContext().getString(R.string.starting_attribute_category_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )
        registryOnProcess.add(registryType)
        val ws = AttributeCategoryWs()
        val categoryRepository = AttributeCategoryRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_attribute_category_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_attribute_category_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.attribute_category_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_route_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = RouteWs()
        val upWs = RouteCompositionWs()

        val routeRepository = RouteRepository()
        val compositionRepository = RouteCompositionRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_route_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_route_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.route_synchronization_completed),
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
                                msg = getContext().getString(R.string.synchronizing_route_contents),
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
                msg = getContext().getString(R.string.starting_data_collection_rule_synchronization),
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

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_data_collection_rule_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_data_collection_rule_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.data_collection_rule_synchronization_completed),
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
                                msg = getContext().getString(R.string.synchronizing_data_collection_rule_contents),
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
                                msg = getContext().getString(R.string.synchronizing_data_collection_rule_targets),
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
                msg = getContext().getString(R.string.starting_maintenance_type_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = MaintenanceTypeWs()
        val typeRepository = MaintenanceTypeRepository()

        val date = prefsGetString(
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
                                    msg = getContext().getString(R.string.synchronizing_maintenance_types),
                                    registryType = registryType,
                                    progressStatus = ProgressStatus.running
                                )
                            )

                            if (!scope.isActive) {
                                onUiEvent(
                                    SyncProgress(
                                        totalTask = 0,
                                        completedTask = 0,
                                        msg = getContext().getString(R.string.canceling_maintenance_type_synchronization),
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

                            if (typeRepository.update(maintenanceType)) {
                                Log.d(
                                    this::class.java.simpleName,
                                    "SQLITE-QUERY-UPDATE-->" + obj.manteinance_type_id
                                )
                            } else {
                                try {
                                    Log.d(
                                        this::class.java.simpleName,
                                        "SQLITE-QUERY-INSERT-->" + obj.manteinance_type_id
                                    )

                                    typeRepository.insert(maintenanceType)
                                } catch (ex: Exception) {
                                    // Posible inserción sobre Id existente
                                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = getContext().getString(R.string.local_error_in_maintenance_type_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_maintenance_type_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.maintenance_type_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_maintenance_type_group_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = ManteinanceTypeGroupWs()
        val groupRepository = MaintenanceTypeGroupRepository()

        val date = prefsGetString(
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
                                    msg = getContext().getString(R.string.synchronizing_maintenance_type_groups),
                                    registryType = registryType,
                                    progressStatus = ProgressStatus.starting
                                )
                            )

                            if (!scope.isActive) {
                                onUiEvent(
                                    SyncProgress(
                                        totalTask = 0,
                                        completedTask = 0,
                                        msg = getContext().getString(R.string.canceling_maintenance_type_group_synchronization),
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

                            if (groupRepository.update(typeGroup)) {
                                Log.d(
                                    this::class.java.simpleName,
                                    "SQLITE-QUERY-UPDATE-->" + obj.maintenanceTypeGroupId
                                )
                            } else {
                                try {
                                    Log.d(
                                        this::class.java.simpleName,
                                        "SQLITE-QUERY-INSERT-->" + obj.maintenanceTypeGroupId
                                    )

                                    groupRepository.insert(typeGroup)
                                } catch (ex: Exception) {
                                    // Posible inserción sobre Id existente
                                    ErrorLog.writeLog(
                                        null, this::class.java.simpleName, ex
                                    )
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        onUiEvent(
                            SyncProgress(
                                msg = getContext().getString(R.string.local_error_in_maintenance_type_group_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_maintenance_type_group_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.maintenance_type_group_synchronization_completed),
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
                msg = getContext().getString(R.string.starting_barcode_label_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            )
        )

        registryOnProcess.add(registryType)
        val ws = BarcodeLabelCustomWs()
        val barcodeRepository = BarcodeLabelCustomRepository()

        val date = prefsGetString(
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
                                msg = getContext().getString(R.string.local_error_in_barcode_label_synchronization),
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
                    msg = getContext().getString(R.string.remote_error_in_barcode_label_synchronization),
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
                    msg = getContext().getString(R.string.synchronization_canceled),
                    registryType = registryType,
                    progressStatus = ProgressStatus.canceled
                )
            )

            else -> {
                onUiEvent(
                    SyncProgress(
                        msg = getContext().getString(R.string.barcode_label_synchronization_completed),
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
                prefsPutString(registries, dbTime)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return false
            }

            return true
        }

        private fun getDateTimeStr(): String {
            var dateTime = ""
            val timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + TIME_FILE_NAME)

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
                        getContext().getString(R.string.failed_to_get_the_date_from_the_file)
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