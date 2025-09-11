package com.example.assetControl.network.clientPackages

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.network.trust.CustomSSLContext
import com.example.assetControl.network.utils.Connection.Companion.isOnline
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.settings.preferences.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class GetClientPackages(
    private val email: String,
    private val password: String,
    private val installationCode: String,
    private val onProgress: (ClientPackagesProgress) -> Unit = {},
) {
    private val urlRequest = "https://config.dacosys.com/configuration/retrieve"

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private suspend fun doInBackground() {
        coroutineScope { suspendFunction() }
    }

    private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
        sendMessage(
            status = ProgressStatus.starting,
            msg = context.getString(R.string.obtaining_client_packages_)
        )

        if (!isOnline()) {
            sendMessage(
                status = ProgressStatus.canceled,
                msg = context.getString(R.string.no_connection)
            )
            return@withContext
        }

        getPackages()
    }

    private fun getPackages() {
        val url: URL
        var connection: HttpsURLConnection? = null

        try {
            //Create connection
            url = URL(urlRequest)

            sendMessage(
                status = ProgressStatus.running,
                msg = context.getString(R.string.obtaining_client_packages_)
            )

            connection = if (Repository.wsUseProxy) {
                val authenticator = object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            Repository.wsProxyUser,
                            Repository.wsProxyPass.toCharArray()
                        )
                    }
                }
                Authenticator.setDefault(authenticator)

                val proxy = Proxy(
                    Proxy.Type.HTTP, InetSocketAddress(
                        Repository.wsProxy,
                        Repository.wsProxyPort
                    )
                )
                url.openConnection(proxy) as HttpsURLConnection
            } else {
                url.openConnection() as HttpsURLConnection
            }

            connection.doOutput = true
            connection.doInput = true
            //connection.instanceFollowRedirects = false
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.connectTimeout = Repository.connectionTimeout * 1000

            val sslContext = CustomSSLContext.createCustomSSLContext()
            connection.sslSocketFactory = sslContext.socketFactory

            //connection.useCaches = false

            val authDataCont = JSONObject()
            authDataCont
                .put("version", "1")
                .put("email", email)
                .put("password", password)

            val jsonParam = JSONObject()
            jsonParam.put("authdata", authDataCont)

            // Mostrar el resultado en logcat
            val utf8JsonString = jsonParam.toString().toByteArray(charset("UTF8"))
            println(utf8JsonString.toString())

            val wr = DataOutputStream(connection.outputStream)
            wr.write(utf8JsonString, 0, utf8JsonString.size)

            wr.flush()
            wr.close()

            getResponse(connection)
        } catch (ex: Exception) {
            sendMessage(
                status = ProgressStatus.crashed,
                msg = "${
                    context.getString(R.string.exception_error)
                }: ${ex.message}"
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun getResponse(connection: HttpsURLConnection) {
        //Get Response
        val result: ArrayList<JSONObject> = ArrayList()

        val inputStream = connection.inputStream
        val rd = BufferedReader(InputStreamReader(inputStream))

        val response = StringBuilder()
        rd.forEachLine { l ->
            response.append(l)
            response.append('\r')
        }

        rd.close()

        try {
            val jsonObj = JSONObject(response.toString())

            if (jsonObj.has("error")) {
                val error = jsonObj.getJSONObject("error")

                val code = error.getString("code")
                val name = error.getString("name")
                val description = error.getString("description")

                sendMessage(
                    status = ProgressStatus.crashed,
                    msg = String.format("%s (%s): %s", name, code, description)
                )
                return
            }

            if (jsonObj.has("packages")) {
                val jsonPackages = jsonObj.getJSONObject("packages")

                for (k in jsonPackages.keys()) {
                    val jsonPack = jsonPackages.getJSONObject(k)
                    if (installationCode.trim().isNotEmpty()) {
                        val tempInstallationCode = jsonPack.getString("installation_code")
                        if (tempInstallationCode != installationCode) {
                            continue
                        }
                    }

                    val productId = jsonPack.getInt("product_version_id")
                    if (jsonPack.getInt("active") == 1 &&
                        (productId == Statics.APP_VERSION_ID || productId == Statics.APP_VERSION_ID_IMAGE_CONTROL)
                    ) {
                        result.add(jsonPack)
                    }
                }
            }

            sendMessage(
                status = ProgressStatus.finished,
                result = result,
                clientEmail = email,
                clientPassword = password,
                msg = if (result.isNotEmpty()) {
                    context.getString(R.string.success_response)
                } else {
                    context
                        .getString(R.string.client_has_no_software_packages)
                }
            )
        } catch (ex: JSONException) {
            Log.e(this::class.java.simpleName, ex.toString())

            sendMessage(
                status = ProgressStatus.crashed,
                msg = "${
                    context.getString(R.string.exception_error)
                }: ${ex.message}"
            )
        }
    }

    private fun sendMessage(
        status: ProgressStatus,
        result: ArrayList<JSONObject> = ArrayList(),
        clientEmail: String = "",
        clientPassword: String = "",
        msg: String = ""
    ) {
        onProgress.invoke(
            ClientPackagesProgress(
                status = status,
                result = result,
                clientEmail = clientEmail,
                clientPassword = clientPassword,
                msg = msg
            )
        )
    }

    init {
        scope.launch {
            doInBackground()
        }
    }
}