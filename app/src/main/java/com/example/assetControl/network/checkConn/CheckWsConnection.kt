package com.example.assetControl.network.checkConn

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.example.assetControl.network.serverDate.GetMySqlDate
import com.example.assetControl.network.serverDate.MySqlDateResult
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType

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
        svm.wsTestUrl = url
        svm.wsTestNamespace = namespace
        svm.wsTestUseProxy = useProxy
        svm.wsTestProxyUrl = proxyUrl
        svm.wsTestProxyPort = proxyPort
        svm.wsTestProxyUser = proxyUser
        svm.wsTestProxyPass = proxyPass

        if (svm.wsTestUrl.isEmpty() || svm.wsTestNamespace.isEmpty()) {
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
                            context
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