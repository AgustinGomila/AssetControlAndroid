package com.dacosys.assetControl.network.checkConn

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.network.webService.moshi.UserAuthResult
import kotlinx.coroutines.*

class ImageControlCheckUser(private var onSnackBarEvent: (SnackBarEventData) -> Unit = {}) {
    private suspend fun onUiEvent(it: SnackBarEventData) {
        withContext(Dispatchers.Main) {
            onSnackBarEvent.invoke(it)
        }
    }

    private fun postExecute(result: UserAuthResult?): UserAuthResult? {
        var fReturn = false
        var fError = false

        when (result) {
            null -> fError = true
            else -> fReturn = result.access
        }

        scope.launch {
            onUiEvent(
                SnackBarEventData(
                    when {
                        fError -> getContext().getString(R.string.connection_error)
                        !fReturn -> getContext().getString(R.string.incorrect_username_password_combination)
                        else -> getContext().getString(R.string.ok)
                    }, when {
                        fError -> SnackBarType.ERROR
                        !fReturn -> SnackBarType.ERROR
                        else -> SnackBarType.SUCCESS
                    }
                )
            )
        }
        return result
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<UserAuthResult?>? = null
    private suspend fun doInBackground(): UserAuthResult? {
        var result: UserAuthResult? = null
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await()
        }
        return postExecute(result)
    }

    private suspend fun suspendFunction(): UserAuthResult? = withContext(Dispatchers.IO) {
        return@withContext try {
            Statics.setupImageControl()
            com.dacosys.imageControl.ImageControl.webservice.imageControlUserCheck()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        }
    }
}