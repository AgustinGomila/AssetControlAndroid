package com.dacosys.assetControl.sync.functions

import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.AssetControl.Companion.getContext
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetWs
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeDbHelper
import com.dacosys.assetControl.model.assets.attributes.attribute.wsObject.AttributeObject
import com.dacosys.assetControl.model.assets.attributes.attribute.wsObject.AttributeWs
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.wsObject.AttributeCategoryWs
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.`object`.AttributeComposition
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.wsObject.AttributeCompositionWs
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryDbHelper
import com.dacosys.assetControl.model.assets.itemCategory.wsObject.ItemCategoryWs
import com.dacosys.assetControl.model.assets.manteinances.manteinanceStatus.dbHelper.ManteinanceStatusDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.`object`.ManteinanceType
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.wsObject.ManteinanceTypeWs
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.`object`.ManteinanceTypeGroup
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.wsObject.ManteinanceTypeGroupWs
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.wsObject.BarcodeLabelCustomWs
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelTarget.dbHelper.BarcodeLabelTargetDbHelper
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseDbHelper
import com.dacosys.assetControl.model.locations.warehouse.wsObject.WarehouseWs
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.wsObject.WarehouseAreaWs
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.wsObject.DataCollectionRuleWs
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.`object`.DataCollectionRuleContent
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.dbHelper.DataCollectionRuleContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.wsObject.DataCollectionRuleContentWs
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.`object`.DataCollectionRuleTarget
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.wsObject.DataCollectionRuleTargetWs
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteDbHelper
import com.dacosys.assetControl.model.routes.route.wsObject.RouteWs
import com.dacosys.assetControl.model.routes.routeComposition.`object`.RouteComposition
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionDbHelper
import com.dacosys.assetControl.model.routes.routeComposition.wsObject.RouteCompositionWs
import com.dacosys.assetControl.model.routes.routeProcessStatus.dbHelper.RouteProcessStatusDbHelper
import com.dacosys.assetControl.model.users.user.dbHelper.UserDbHelper
import com.dacosys.assetControl.model.users.user.wsObject.UserWs
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionDbHelper
import com.dacosys.assetControl.model.users.userPermission.wsObject.UserPermissionObject
import com.dacosys.assetControl.model.users.userPermission.wsObject.UserPermissionWs
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaDbHelper
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaObject
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaWs
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class SyncDownload {
    private var weakReference: WeakReference<SyncTaskProgress>? = null
    private var syncTaskProgress: SyncTaskProgress?
        get() {
            return weakReference?.get()
        }
        set(value) {
            weakReference = if (value != null) WeakReference(value) else null
        }

    private var weakReference2: WeakReference<Statics.SessionCreated>? = null
    private var sessionCreated: Statics.SessionCreated?
        get() {
            return weakReference2?.get()
        }
        set(value) {
            weakReference2 = if (value != null) WeakReference(value) else null
        }

    private var registryOnProcess: ArrayList<SyncRegistryType> = ArrayList()
    private var cancelNow: Boolean = false
    private var registryTypeUpdated = ArrayList<SyncRegistryType>()
    private var serverTime: String = ""

    fun addParams(
        callback: WeakReference<SyncTaskProgress>,
        callback2: WeakReference<Statics.SessionCreated>,
    ) {
        this.weakReference = callback
        this.weakReference2 = callback2
    }

    fun execute() {
        checkConnection()
    }

    private fun checkConnection() {
        thread {
            val getMySqlDate = GetMySqlDate()
            val r = getMySqlDate.execute(Statics.getWebservice())

            if (r.status == ProgressStatus.finished) {
                this.serverTime = r.msg
                val result = doInBackground()
                if (result) {
                    syncTaskProgress?.onSyncTaskProgress(
                        0,
                        0,
                        getContext().getString(R.string.download_finished),
                        null,
                        ProgressStatus.bigFinished
                    )
                } else {
                    syncTaskProgress?.onSyncTaskProgress(
                        0,
                        0,
                        getContext().getString(R.string.error_downloading_the_database_from_the_server),
                        null,
                        ProgressStatus.crashed
                    )
                }
            } else if (
                r.status == ProgressStatus.crashed ||
                r.status == ProgressStatus.canceled
            ) {
                syncTaskProgress?.onSyncTaskProgress(0, 0, serverTime, null, r.status)
            }
        }
    }

    private var deferred: Deferred<Boolean>? = null

    private fun doInBackground(): Boolean {
        var result = false
        runBlocking {
            deferred = async { suspendFunction(sessionCreated) }
            result = deferred?.await() ?: false
        }
        return result
    }

    private suspend fun suspendFunction(sessionCreatedListener: Statics.SessionCreated?): Boolean =
        withContext(Dispatchers.IO) {
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.downloading_starting),
                null,
                ProgressStatus.bigStarting
            )

            qty = Statics.prefsGetInt(ConfEntry.acSyncQtyRegistry)

            if (Statics.currentSession == null && sessionCreatedListener != null) {
                thread {
                    val setSession = SetCurrentSession()
                    setSession.addParams(sessionCreatedListener)
                    setSession.execute()
                }
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

                if (Statics.prefsGetBoolean(Preference.useAssetControlManteinance)) {
                    t.union(
                        listOf(
                            async { manteinanceType() },
                            async { manteinanceTypeGroup() })
                    )
                }

                // De esta manera se mantiene la ejecución de los threads
                // dentro del bucle y sale recién cuando terminó con el último

                t.awaitAll()

            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.synchronization_failed),
                    null,
                    ProgressStatus.bigCrashed
                )
                return@withContext false
            } finally {
                saveLastUpdateDates()
            }

            return@withContext true
        }

    fun forceCancel() {
        cancelNow = true
    }

    private fun saveLastUpdateDates(): Boolean {
        try {
            if (serverTime.isNotEmpty() && !cancelNow) {
                val registries: ArrayList<String> = ArrayList()
                for (registryType in registryTypeUpdated) {
                    if (registryType.confEntry == null) continue
                    Log.d(
                        this::class.java.simpleName,
                        "${
                            getContext()
                                .getString(R.string.saving_synchronization_time)
                        }: ${registryType.confEntry!!.description} (${serverTime})"
                    )
                    registries.add(registryType.confEntry!!.description)
                }
                Statics.prefsPutString(registries, serverTime)
            }
            registryTypeUpdated.clear()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        }
        return true
    }

    private fun asset() {
        val registryType = SyncRegistryType.Asset
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_asset_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = AssetWs()
        val aDb = AssetDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.assetGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        aDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_asset_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_asset_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.asset_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun itemCategory() {
        val registryType = SyncRegistryType.ItemCategory
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_category_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = ItemCategoryWs()
        val icDb = ItemCategoryDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.itemCategoryGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        icDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_category_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_category_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.category_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun user() {
        val registryType = SyncRegistryType.User
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_user_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = UserWs()
        val upWs = UserPermissionWs()
        val uwaWs = UserWarehouseAreaWs()

        val db = UserDbHelper()
        val upDb = UserPermissionDbHelper()
        val uwaDb = UserWarehouseAreaDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.userGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        if (objArray.isNotEmpty()) {
                            db.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                            currentCount += objArray.size

                            for (obj in objArray) {
                                // user permission
                                userPermission(
                                    upWs.userPermissionGet(obj.user_id),
                                    upDb,
                                    obj.user_id
                                )

                                // user warehouse area
                                userWarehouseArea(
                                    uwaWs.userWarehouseAreaGet(obj.user_id),
                                    uwaDb,
                                    obj.user_id
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_user_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_user_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.user_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun warehouse() {
        val registryType = SyncRegistryType.Warehouse
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_warehouse_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseWs()
        val wDb = WarehouseDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.warehouseGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        wDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_warehouse_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_warehouse_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.warehouse_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun warehouseArea() {
        val registryType = SyncRegistryType.WarehouseArea
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_warehouse_area_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = WarehouseAreaWs()
        val waDb = WarehouseAreaDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.warehouseAreaGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        waDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_warehouse_area_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_warehouse_area_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.warehouse_area_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun attribute() {
        val registryType = SyncRegistryType.Attribute
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_attribute_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = AttributeWs()
        val upWs = AttributeCompositionWs()

        val aDb = AttributeDbHelper()
        val acDb = AttributeCompositionDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.attributeGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        aDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                        attributeComposition(
                            ws = upWs,
                            aDb = acDb,
                            attr = objArray,
                            callback = syncTaskProgress ?: return
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_attribute_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_attribute_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.attribute_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun attributeComposition(
        ws: AttributeCompositionWs,
        aDb: AttributeCompositionDbHelper,
        attr: Array<AttributeObject>,
        callback: SyncTaskProgress,
    ) {
        try {
            val ids = ArrayList<Long>()
            attr.mapTo(ids) { it.attributeId }
            aDb.deleteByAttrIdArray(ids)

            val allAttrComp: ArrayList<AttributeComposition> = ArrayList()
            var currentCount = 0
            for (a in attr) {
                currentCount++
                callback.onSyncTaskProgress(
                    totalTask = attr.size,
                    completedTask = currentCount,
                    msg = getContext()
                        .getString(R.string.synchronizing_attribute_compositions),
                    registryType = SyncRegistryType.AttributeComposition,
                    progressStatus = ProgressStatus.running
                )

                val attributeId = a.attributeId
                val objArray = ws.attributeCompositionGet(attributeId)
                if (objArray != null && objArray.isNotEmpty()) {
                    for (obj in objArray) {
                        val attributeComposition = AttributeComposition(
                            obj.attributeCompositionId,
                            obj.attributeId,
                            obj.attributeCompositionTypeId,
                            obj.description,
                            obj.composition,
                            obj.used == 1,
                            obj.name,
                            obj.readOnly == 1,
                            obj.defaultValue
                        )

                        Log.d(
                            this::class.java.simpleName,
                            "SQLITE-QUERY-INSERT-->" + attributeId + "," + obj.attributeCompositionId
                        )
                        allAttrComp.add(attributeComposition)
                    }
                }
            }
            aDb.insertChunked(allAttrComp)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private fun attributeCategory() {
        val registryType = SyncRegistryType.AttributeCategory
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_attribute_category_synchronization),
            registryType,
            ProgressStatus.starting
        )
        registryOnProcess.add(registryType)
        val ws = AttributeCategoryWs()
        val acDb = AttributeCategoryDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.attributeCategoryGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        acDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_attribute_category_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_attribute_category_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.attribute_category_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun route() {
        val registryType = SyncRegistryType.Route
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_route_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = RouteWs()
        val upWs = RouteCompositionWs()

        val rDb = RouteDbHelper()
        val rcDb = RouteCompositionDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.routeGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        rDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size

                        for (obj in objArray) {
                            // route composition
                            routeComposition(
                                ws = upWs,
                                aDb = rcDb,
                                routeId = obj.route_id,
                                callback = syncTaskProgress ?: return
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_route_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_route_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.route_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun routeComposition(
        ws: RouteCompositionWs,
        aDb: RouteCompositionDbHelper,
        routeId: Long,
        callback: SyncTaskProgress,
    ) {
        try {
            val objArray = ws.routeCompositionGet(routeId)
            aDb.deleteByRouteId(routeId)

            if (objArray != null && objArray.isNotEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val rc: ArrayList<RouteComposition> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        callback.onSyncTaskProgress(
                            totalTask = countTotal,
                            completedTask = currentCount,
                            msg = getContext()
                                .getString(R.string.synchronizing_route_contents),
                            registryType = SyncRegistryType.RouteComposition,
                            progressStatus = ProgressStatus.running
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
                    aDb.insert(rc)
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

    private fun dataCollectionRule() {
        val registryType = SyncRegistryType.DataCollectionRule
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_data_collection_rule_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = DataCollectionRuleWs()
        val upWs = DataCollectionRuleContentWs()
        val uwaWs = DataCollectionRuleTargetWs()

        val dcrDb = DataCollectionRuleDbHelper()
        val dcrcDb = DataCollectionRuleContentDbHelper()
        val dcrtDb = DataCollectionRuleTargetDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.dataCollectionRuleGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        dcrDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size

                        for (obj in objArray) {
                            // dataCollectionRule content
                            dataCollectionRuleContent(
                                ws = upWs,
                                aDb = dcrcDb,
                                dataCollectionRuleId = obj.dataCollectionRuleId,
                                callback = syncTaskProgress ?: return
                            )

                            // dataCollectionRule warehouse area
                            dataCollectionRuleTarget(
                                ws = uwaWs,
                                aDb = dcrtDb,
                                dataCollectionRuleId = obj.dataCollectionRuleId,
                                callback = syncTaskProgress ?: return
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_data_collection_rule_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_data_collection_rule_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.data_collection_rule_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun dataCollectionRuleContent(
        ws: DataCollectionRuleContentWs,
        aDb: DataCollectionRuleContentDbHelper,
        dataCollectionRuleId: Long,
        callback: SyncTaskProgress,
    ) {
        try {
            val objArray = ws.dataCollectionRuleContentGet(dataCollectionRuleId)
            aDb.deleteByDataCollectionRuleId(dataCollectionRuleId)

            if (objArray != null && objArray.isNotEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val dcrc: ArrayList<DataCollectionRuleContent> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        callback.onSyncTaskProgress(
                            totalTask = countTotal,
                            completedTask = currentCount,
                            msg = getContext()
                                .getString(R.string.synchronizing_data_collection_rule_contents),
                            registryType = SyncRegistryType.DataCollectionRuleContent,
                            progressStatus = ProgressStatus.running
                        )

                        val dataCollectionRuleContent = DataCollectionRuleContent(
                            dataCollectionRuleContentId = obj.dataCollectionRuleContentId,
                            description = obj.description,
                            dataCollectionRuleId = obj.dataCollectionRuleId,
                            level = obj.level,
                            position = obj.position,
                            attributeId = obj.attributeId,
                            attributeCompositionId = obj.attributeCompositionId,
                            expression = obj.expression,
                            trueResult = obj.trueResult,
                            falseResult = obj.falseResult,
                            active = obj.active == 1,
                            mandatory = obj.mandatory == 1
                        )
                        dcrc.add(dataCollectionRuleContent)
                    }
                    aDb.insert(dcrc)
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

    private fun dataCollectionRuleTarget(
        ws: DataCollectionRuleTargetWs,
        aDb: DataCollectionRuleTargetDbHelper,
        dataCollectionRuleId: Long,
        callback: SyncTaskProgress,
    ) {
        try {
            val objArray = ws.dataCollectionRuleTargetGet(dataCollectionRuleId)
            aDb.deleteByDataCollectionRuleId(dataCollectionRuleId)

            if (objArray != null && objArray.isNotEmpty()) {
                val countTotal = objArray.size
                var currentCount = 0
                try {
                    val dcrt: ArrayList<DataCollectionRuleTarget> = ArrayList()
                    for (obj in objArray) {
                        currentCount++
                        callback.onSyncTaskProgress(
                            totalTask = countTotal,
                            completedTask = currentCount,
                            msg = getContext()
                                .getString(R.string.synchronizing_data_collection_rule_targets),
                            registryType = SyncRegistryType.DataCollectionRuleTarget,
                            progressStatus = ProgressStatus.running
                        )

                        val dataCollectionRuleTarget = DataCollectionRuleTarget(
                            dataCollectionRuleId = obj.dataCollectionRuleId,
                            assetId = obj.assetId,
                            warehouseId = obj.warehouseId,
                            warehouseAreaId = obj.warehouseAreaId,
                            itemCategoryId = obj.itemCategoryId
                        )
                        dcrt.add(dataCollectionRuleTarget)
                    }
                    aDb.insert(dcrt)
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

    private fun manteinanceType() {
        val registryType = SyncRegistryType.ManteinanceType
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_maintenance_type_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = ManteinanceTypeWs()
        val mtDb = ManteinanceTypeDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

        val countTotal: Int?
        var errorOccurred = false
        var groupCount = 0
        var currentCount = 0
        var pos = 0
        try {
            countTotal = ws.manteinanceTypeCount(date)
            if (countTotal == null) {
                errorOccurred = true
            } else {
                while (groupCount < countTotal) {
                    val objArray = ws.manteinanceTypeGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        for (obj in objArray) {
                            currentCount++
                            syncTaskProgress?.onSyncTaskProgress(
                                countTotal,
                                currentCount,
                                getContext()
                                    .getString(R.string.synchronizing_maintenance_types),
                                registryType,
                                ProgressStatus.running
                            )

                            if (cancelNow) {
                                syncTaskProgress?.onSyncTaskProgress(
                                    0,
                                    0,
                                    getContext()
                                        .getString(R.string.canceling_maintenance_type_synchronization),
                                    registryType,
                                    ProgressStatus.canceled
                                )
                                return
                            }

                            val manteinanceType = ManteinanceType(
                                obj.manteinance_type_id,
                                obj.description,
                                obj.active == 1,
                                obj.manteinance_type_group_id
                            )

                            if (mtDb.update(manteinanceType)) {
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

                                    mtDb.insert(manteinanceType)
                                } catch (ex: Exception) {
                                    // Posible inserción sobre Id existente
                                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_maintenance_type_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_maintenance_type_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.maintenance_type_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun manteinanceTypeGroup() {
        val registryType = SyncRegistryType.ManteinanceTypeGroup
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_maintenance_type_group_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = ManteinanceTypeGroupWs()
        val mtgDb = ManteinanceTypeGroupDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.manteinanceTypeGroupGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        for (obj in objArray) {
                            currentCount++
                            syncTaskProgress?.onSyncTaskProgress(
                                countTotal,
                                currentCount,
                                getContext()
                                    .getString(R.string.synchronizing_maintenance_type_groups),
                                registryType,
                                ProgressStatus.starting
                            )

                            if (cancelNow) {
                                syncTaskProgress?.onSyncTaskProgress(
                                    0,
                                    0,
                                    getContext()
                                        .getString(R.string.canceling_maintenance_type_group_synchronization),
                                    registryType,
                                    ProgressStatus.canceled
                                )
                                return
                            }

                            val manteinanceTypeGroup = ManteinanceTypeGroup(
                                obj.manteinanceTypeGroupId,
                                obj.description,
                                obj.active == 1
                            )

                            if (mtgDb.update(manteinanceTypeGroup)) {
                                Log.d(
                                    this::class.java.simpleName,
                                    "SQLITE-QUERY-UPDATE-->" + obj.manteinanceTypeGroupId
                                )
                            } else {
                                try {
                                    Log.d(
                                        this::class.java.simpleName,
                                        "SQLITE-QUERY-INSERT-->" + obj.manteinanceTypeGroupId
                                    )

                                    mtgDb.insert(manteinanceTypeGroup)
                                } catch (ex: Exception) {
                                    // Posible inserción sobre Id existente
                                    ErrorLog.writeLog(
                                        null,
                                        this::class.java.simpleName,
                                        ex
                                    )
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_maintenance_type_group_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_maintenance_type_group_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.maintenance_type_group_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    private fun barcodeLabelCustom() {
        val registryType = SyncRegistryType.BarcodeLabelCustom
        syncTaskProgress?.onSyncTaskProgress(
            0,
            0,
            getContext().getString(R.string.starting_barcode_label_synchronization),
            registryType,
            ProgressStatus.starting
        )

        registryOnProcess.add(registryType)
        val ws = BarcodeLabelCustomWs()
        val blcDb = BarcodeLabelCustomDbHelper()

        val date = Statics.prefsGetString(registryType.confEntry ?: return)

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
                    val objArray = ws.barcodeLabelCustomGetAllLimit(
                        pos,
                        qty,
                        date
                    )
                    if (!objArray.any()) break

                    groupCount += objArray.size
                    pos++

                    if (cancelNow) {
                        break
                    }

                    try {
                        blcDb.sync(objArray, syncTaskProgress ?: return, currentCount, countTotal)
                        currentCount += objArray.size
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // Error local
                        syncTaskProgress?.onSyncTaskProgress(
                            0,
                            0,
                            getContext()
                                .getString(R.string.local_error_in_barcode_label_synchronization),
                            registryType,
                            ProgressStatus.crashed
                        )
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext()
                    .getString(R.string.remote_error_in_barcode_label_synchronization),
                registryType,
                ProgressStatus.crashed
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
        }

        // Actualizar fecha de sincronización
        when {
            cancelNow -> syncTaskProgress?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.synchronization_canceled),
                registryType,
                ProgressStatus.canceled
            )
            else -> {
                syncTaskProgress?.onSyncTaskProgress(
                    0,
                    0,
                    getContext()
                        .getString(R.string.barcode_label_synchronization_completed),
                    registryType,
                    ProgressStatus.success
                )
                if (!errorOccurred) {
                    registryTypeUpdated.add(registryType)
                }
            }
        }
    }

    companion object {
        fun insertStatics() {
            AssetReviewStatusDbHelper().sync()
            RouteProcessStatusDbHelper().sync()
            ManteinanceStatusDbHelper().sync()
            BarcodeLabelTargetDbHelper().sync()
        }

        private val registryType = SyncRegistryType.User

        private var weakReference: WeakReference<SyncTaskProgress>? = null
        private var callback: SyncTaskProgress?
            get() {
                return weakReference?.get()
            }
            set(value) {
                weakReference = if (value != null) WeakReference(value) else null
            }

        ////////////////////////////////////////////////////
        // Descarga de usuarios, permisos, áreas del usuario
        // previo a iniciar sesión
        fun initialUser(listener: SyncTaskProgress) {
            this.callback = listener

            val qty = Statics.prefsGetInt(ConfEntry.acSyncQtyRegistry)

            callback?.onSyncTaskProgress(
                0,
                0,
                getContext().getString(R.string.starting_user_synchronization),
                registryType,
                ProgressStatus.starting
            )

            val ws = UserWs()
            val upWs = UserPermissionWs()
            val uwaWs = UserWarehouseAreaWs()

            val db = UserDbHelper()
            val upDb = UserPermissionDbHelper()
            val uwaDb = UserWarehouseAreaDbHelper()

            val date = (registryType.confEntry ?: return).defaultValue.toString()

            val countTotal: Int?

            var errorMsg = ""
            var errorOccurred = false

            var currentCount = 0
            var groupCount = 0
            var pos = 0
            try {
                countTotal = ws.initialUserCount(date)
                if (countTotal == null) {
                    errorMsg = getContext().getString(R.string.no_users)
                    errorOccurred = true
                } else {
                    while (groupCount < countTotal) {
                        val objArray = ws.initialUserGetAllLimit(
                            pos,
                            qty,
                            date
                        )
                        if (!objArray.any()) break

                        groupCount += objArray.size
                        pos++

                        try {
                            if (objArray.isNotEmpty()) {
                                db.sync(
                                    objArray = objArray,
                                    callback = callback,
                                    currentCount = currentCount,
                                    countTotal = countTotal
                                )

                                currentCount += objArray.size

                                for (obj in objArray) {
                                    // user permission
                                    userPermission(
                                        upWs.initialUserPermissionGet(obj.user_id),
                                        upDb,
                                        obj.user_id
                                    )

                                    // user warehouse area
                                    userWarehouseArea(
                                        uwaWs.initialUserWarehouseAreaGet(obj.user_id),
                                        uwaDb,
                                        obj.user_id
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            ErrorLog.writeLog(null, this::class.java.simpleName, ex)

                            // Error local
                            errorMsg =
                                getContext().getString(R.string.local_error_in_user_synchronization)
                            errorOccurred = true

                            break
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)

                // Error remoto
                errorMsg = getContext().getString(R.string.remote_error_in_user_synchronization)
                errorOccurred = true
            }

            if (errorOccurred) {
                callback?.onSyncTaskProgress(
                    0,
                    0,
                    errorMsg,
                    registryType,
                    ProgressStatus.crashed
                )
                return
            }

            checkConnection()
        }

        private fun checkConnection() {
            thread {
                val getMySqlDate = GetMySqlDate()
                val r = getMySqlDate.execute(Statics.getWebservice())

                if (r.status == ProgressStatus.finished) {
                    Log.d(
                        this::class.java.simpleName,
                        "${getContext().getString(R.string.saving_synchronization_time)}: ${(registryType.confEntry ?: return@thread).description} ($r.msg)"
                    )
                    Statics.prefsPutString(
                        (registryType.confEntry ?: return@thread).description,
                        r.msg
                    )
                    callback?.onSyncTaskProgress(
                        0,
                        0,
                        getContext().getString(R.string.ok),
                        registryType,
                        ProgressStatus.finished
                    )
                } else if (r.status == ProgressStatus.crashed || r.status == ProgressStatus.canceled) {
                    callback?.onSyncTaskProgress(0, 0, r.msg, registryType, r.status)
                }
            }
        }

        private fun userPermission(
            objArray: Array<UserPermissionObject>?,
            aDb: UserPermissionDbHelper,
            userId: Long,
        ) {
            try {
                aDb.deleteByUserId(userId)
                try {
                    if (objArray != null && objArray.isNotEmpty()) {
                        aDb.insert(objArray)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    // Error local
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    return
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                // Error remoto
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return
            }
        }

        private fun userWarehouseArea(
            objArray: Array<UserWarehouseAreaObject>?,
            aDb: UserWarehouseAreaDbHelper,
            userId: Long,
        ) {
            try {
                aDb.deleteByUserId(userId)
                try {
                    if (objArray != null && objArray.isNotEmpty()) {
                        aDb.insert(objArray)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    // Error local
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    return
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                // Error remoto
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return
            }
        }

        private fun getDateTimeStr(): String {
            var dateTime = ""
            val timeFileLocation =
                File(getContext().cacheDir.absolutePath + "/" + DownloadDb.timeFilename)

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
                    this::class.java.simpleName,
                    "${
                        getContext()
                            .getString(R.string.failed_to_get_the_date_from_the_file)
                    }: ${ex.message}"
                )
            }
            return dateTime
        }

        // endregion InitialUserSync

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
                Statics.prefsPutString(registries, dbTime)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return false
            }

            return true
        }
    }
}