package com.example.assetControl.network.serverDate

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.webservice.common.Webservice
import com.example.assetControl.network.utils.Connection.Companion.isOnline
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetMySqlDate(ws: Webservice, private var onResult: (MySqlDateResult) -> Unit = {}) {
    private var webservice: Webservice = ws

    fun execute() {
        if (isOnline()) launchRequest()
        else {
            onResult.invoke(
                MySqlDateResult(
                    ProgressStatus.canceled, context.getString(R.string.no_connection)
                )
            )
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private fun launchRequest() {
        scope.launch {
            val result = doInBackground()
            if (result == "") onResult.invoke(MySqlDateResult(ProgressStatus.crashed, result))
            else onResult.invoke(MySqlDateResult(ProgressStatus.finished, result))
        }
    }

    private var deferred: Deferred<String>? = null
    private suspend fun doInBackground(): String {
        var result = ""
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: ""
        }
        return result
    }

    private suspend fun suspendFunction(): String = withContext(Dispatchers.IO) {
        return@withContext webservice.mysqlDate()
    }
}