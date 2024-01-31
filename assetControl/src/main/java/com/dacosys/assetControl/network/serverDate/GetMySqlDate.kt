package com.dacosys.assetControl.network.serverDate

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.webservice.common.Webservice
import com.dacosys.assetControl.network.utils.Connection.Companion.isOnline
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.*

class GetMySqlDate(ws: Webservice, private var onResult: (MySqlDateResult) -> Unit = {}) {
    private var webservice: Webservice = ws

    fun execute() {
        if (isOnline()) launchRequest()
        else {
            onResult.invoke(
                MySqlDateResult(
                    ProgressStatus.canceled, getContext().getString(R.string.no_connection)
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