package com.dacosys.assetControl.network.checkConn

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.common.Webservice.Companion.getWebservice

class CheckWsConnection(
    private var url: String,
    private var namespace: String,
    private var onSnackBarEvent: (SnackBarEventData) -> Unit = {},
) {
    private var useProxy: Boolean = false
    private var proxyUrl: String = ""
    private var proxyPort: Int = 0
    private var proxyUser: String = ""
    private var proxyPass: String = ""

    fun addProxyParams(
        useProxy: Boolean,
        proxyUrl: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
    ) {
        this.useProxy = useProxy
        this.proxyUrl = proxyUrl
        this.proxyPort = proxyPort
        this.proxyUser = proxyUser
        this.proxyPass = proxyPass
    }

    fun execute() {
        Statics.wsTestUrl = url
        Statics.wsTestNamespace = namespace
        Statics.wsTestUseProxy = useProxy
        Statics.wsTestProxyUrl = proxyUrl
        Statics.wsTestProxyPort = proxyPort
        Statics.wsTestProxyUser = proxyUser
        Statics.wsTestProxyPass = proxyPass

        if (Statics.wsTestUrl.isEmpty() || Statics.wsTestNamespace.isEmpty()) {
            return
        }

        checkConnection()
    }

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            when (it.status) {
                ProgressStatus.crashed -> {
                    onSnackBarEvent.invoke(SnackBarEventData(it.msg, SnackBarType.ERROR))
                }
                ProgressStatus.canceled -> {
                    onSnackBarEvent.invoke(SnackBarEventData(it.msg, SnackBarType.INFO))
                }
                ProgressStatus.finished -> {
                    onSnackBarEvent.invoke(
                        SnackBarEventData(
                            getContext()
                                .getString(R.string.ok),
                            SnackBarType.SUCCESS
                        )
                    )
                }
            }
        }
        GetMySqlDate(getWebservice()) { onConnectionResult(it) }.execute()
    }
}