package com.dacosys.assetControl.network.sync

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.repository.user.UserPermissionRepository
import com.dacosys.assetControl.data.room.repository.user.UserRepository
import com.dacosys.assetControl.data.room.repository.user.UserWarehouseAreaRepository
import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.user.*
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutString
import kotlinx.coroutines.*

class SyncInitialUser(
    private var onSyncTaskProgress: (SyncProgress) -> Unit = {},
) {
    private val registryType = SyncRegistryType.User

    private val ws get() = UserWs()
    private val userRepository get() = UserRepository()
    private val userPermissionRepository get() = UserPermissionRepository()
    private val userWarehouseAreaRepository get() = UserWarehouseAreaRepository()

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private suspend fun doInBackground() {
        coroutineScope {
            launch {
                // Eliminar datos antiguos de los usuarios
                userRepository.deleteAll()
                userPermissionRepository.deleteAll()
                userWarehouseAreaRepository.deleteAll()

                initialUser()
            }
        }
    }

    ////////////////////////////////////////////////////
    // Descarga de usuarios, permisos, áreas del usuario
    // previo a iniciar sesión
    private suspend fun initialUser() = withContext(Dispatchers.IO) {
        val qty =
            prefsGetInt(ConfEntry.acSyncQtyRegistry)

        scope.launch {
            onUiEvent(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_user_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )
        }

        val confEntry = registryType.confEntry ?: return@withContext
        val date = confEntry.defaultValue.toString()

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

                    val objArray = ws.initialUserGetAllLimit(pos, qty, date)
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

                            val total = objArray.size
                            currentCount += total
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
            scope.launch {
                onUiEvent(
                    SyncProgress(
                        msg = errorMsg,
                        registryType = registryType,
                        progressStatus = ProgressStatus.crashed
                    )
                )
            }
            return@withContext
        }

        checkConnection()
    }

    private fun checkConnection() {
        GetMySqlDate(getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun onConnectionResult(it: MySqlDateResult) {
        val confEntry = registryType.confEntry ?: return

        when (it.status) {
            ProgressStatus.finished -> {
                Log.d(
                    this::class.java.simpleName, "${
                        getContext().getString(R.string.saving_synchronization_time)
                    }: ${confEntry.description} ($it.msg)"
                )
                prefsPutString(confEntry.description, it.msg)
                scope.launch {
                    onUiEvent(
                        SyncProgress(
                            msg = getContext().getString(R.string.ok),
                            registryType = registryType,
                            progressStatus = ProgressStatus.finished
                        )
                    )
                }
            }

            ProgressStatus.crashed, ProgressStatus.canceled -> {
                scope.launch {
                    onUiEvent(
                        SyncProgress(
                            msg = it.msg,
                            registryType = registryType,
                            progressStatus = it.status
                        )
                    )
                }
            }
        }
    }

    private suspend fun onUiEvent(it: SyncProgress) {
        withContext(Dispatchers.Main) {
            onSyncTaskProgress.invoke(it)
        }
    }

    init {
        scope.launch {
            doInBackground()
        }
    }
}