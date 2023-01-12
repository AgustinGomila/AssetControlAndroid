package com.dacosys.assetControl.network.serverDate

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.Webservice
import kotlinx.coroutines.*

class GetMySqlDate(ws: Webservice, private var onResult: (MySqlDateResult) -> Unit = {}) {
    private var webservice: Webservice = ws

    fun execute() {
        if (Statics.isOnline()) launchRequest()
        else {
            onResult.invoke(
                MySqlDateResult(
                    ProgressStatus.canceled,
                    getContext().getString(R.string.no_connection)
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
            if (result == "")
                onResult.invoke(MySqlDateResult(ProgressStatus.crashed, result))
            else
                onResult.invoke(MySqlDateResult(ProgressStatus.finished, result))
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