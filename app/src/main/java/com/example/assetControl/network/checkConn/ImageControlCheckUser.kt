package com.example.assetControl.network.checkConn

import com.dacosys.imageControl.dto.UserAuthResult
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.imageControl
import com.example.assetControl.R
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.imageControl.ImageControl.Companion.setupImageControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                        fError -> context.getString(R.string.connection_error)
                        !fReturn -> context.getString(R.string.incorrect_username_password_combination)
                        else -> context.getString(R.string.ok)
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
            setupImageControl()
            imageControl.webservice.imageControlUserCheck()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        }
    }
}