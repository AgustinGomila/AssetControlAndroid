package com.dacosys.assetControl.sync.functions

import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.AssetControl.Companion.getContext
import com.dacosys.assetControl.wsGeneral.Webservice
import kotlinx.coroutines.*

class GetMySqlDate {
    private lateinit var webservice: Webservice

    fun execute(ws: Webservice): MySqlDateResult {
        if (!Statics.isOnline()) {
            return MySqlDateResult(
                ProgressStatus.canceled,
                getContext().getString(R.string.no_connection)
            )
        }

        this.webservice = ws
        val result = doInBackground()

        return if (result == "")
            MySqlDateResult(ProgressStatus.crashed, result)
        else
            MySqlDateResult(ProgressStatus.finished, result)
    }

    private var deferred: Deferred<String>? = null
    private fun doInBackground(): String {
        var result = ""
        runBlocking {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: ""
        }
        return result
    }

    private suspend fun suspendFunction(): String = withContext(Dispatchers.IO) {
        return@withContext webservice.mysqlDate()
    }

    companion object {

        /**
         * status: Cómo termino el proceso de obtención de la fecha en el servidor
         * msg: Si status es finished, msg es la fecha devuelta por el servidor. Sino es la descripción del error.
         */

        class MySqlDateResult(status: ProgressStatus, msg: String) {
            var status: ProgressStatus = ProgressStatus.unknown
            var msg: String = ""

            init {
                this.status = status
                this.msg = msg
            }
        }
    }
}