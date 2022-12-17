package com.dacosys.assetControl.network.sync

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.users.user.dbHelper.UserDbHelper
import com.dacosys.assetControl.model.users.user.wsObject.UserWs
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionDbHelper
import com.dacosys.assetControl.model.users.userPermission.wsObject.UserPermissionObject
import com.dacosys.assetControl.model.users.userPermission.wsObject.UserPermissionWs
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaDbHelper
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaObject
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaWs
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.*

class SyncInitialUser(
    private var onSyncTaskProgress: (SyncProgress) -> Unit = {},
) {
    private val registryType = SyncRegistryType.User

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
        return@withContext initialUser()
    }

    ////////////////////////////////////////////////////
    // Descarga de usuarios, permisos, áreas del usuario
    // previo a iniciar sesión
    private fun initialUser() {
        val qty = Statics.prefsGetInt(ConfEntry.acSyncQtyRegistry)

        scope.launch {
            onUiEvent(SyncProgress(
                totalTask = 0,
                completedTask = 0,
                msg = getContext()
                    .getString(R.string.starting_user_synchronization),
                registryType = registryType,
                progressStatus = ProgressStatus.starting
            ))
        }

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
                    if (!scope.isActive) break

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
                            db.sync(objArray = objArray,
                                onSyncTaskProgress = { onSyncTaskProgress.invoke(it) },
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
                            getContext()
                                .getString(R.string.local_error_in_user_synchronization)
                        errorOccurred = true

                        break
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)

            // Error remoto
            errorMsg = getContext()
                .getString(R.string.remote_error_in_user_synchronization)
            errorOccurred = true
        }

        if (errorOccurred) {
            scope.launch {
                onUiEvent(SyncProgress(
                    totalTask = 0,
                    completedTask = 0,
                    msg = errorMsg,
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                ))
            }
            return
        }

        checkConnection()
    }

    private fun checkConnection() {
        GetMySqlDate(Statics.getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun onConnectionResult(it: MySqlDateResult) {
        when (it.status) {
            ProgressStatus.finished -> {
                Log.d(
                    this::class.java.simpleName,
                    "${
                        getContext()
                            .getString(R.string.saving_synchronization_time)
                    }: ${(registryType.confEntry ?: return).description} ($it.msg)"
                )
                Statics.prefsPutString(
                    (registryType.confEntry ?: return).description,
                    it.msg
                )
                scope.launch {
                    onUiEvent(SyncProgress(
                        totalTask = 0,
                        completedTask = 0,
                        msg = getContext().getString(R.string.ok),
                        registryType = registryType,
                        progressStatus = ProgressStatus.finished
                    ))
                }
            }
            ProgressStatus.crashed, ProgressStatus.canceled -> {
                scope.launch {
                    onUiEvent(SyncProgress(
                        totalTask = 0,
                        completedTask = 0,
                        msg = it.msg,
                        registryType = registryType,
                        progressStatus = it.status))
                }
            }
        }
    }

    fun userPermission(
        objArray: Array<UserPermissionObject>?,
        aDb: UserPermissionDbHelper,
        userId: Long,
    ) {
        try {
            aDb.deleteByUserId(userId)
            if (objArray != null && objArray.isNotEmpty()) {
                aDb.insert(objArray)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    fun userWarehouseArea(
        objArray: Array<UserWarehouseAreaObject>?,
        aDb: UserWarehouseAreaDbHelper,
        userId: Long,
    ) {
        try {
            aDb.deleteByUserId(userId)
            if (objArray != null && objArray.isNotEmpty()) {
                aDb.insert(objArray)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        }
    }

    private suspend fun onUiEvent(it: SyncProgress) {
        withContext(Dispatchers.Main) {
            onSyncTaskProgress.invoke(it)
        }
    }

    // endregion InitialUserSync

    init {
        scope.launch {
            doInBackground()
        }
    }
}