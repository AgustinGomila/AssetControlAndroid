package com.example.assetControl.data.webservice.common

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.network.trust.CustomSSLContext
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.misc.Md5
import com.example.assetControl.utils.settings.config.Preference
import org.ksoap2.HeaderProperty
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import org.ksoap2.transport.HttpsServiceConnectionSE
import org.ksoap2.transport.HttpsTransportSE
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.util.*
import java.util.regex.Pattern


class Webservice @Throws(Exception::class) constructor(private var webServiceType: WebServiceType?) {
    companion object {
        private var wsInitialized = false
        lateinit var ws: Webservice

        fun getWebservice(): Webservice {
            if (!wsInitialized) {
                ws = Webservice(WebServiceType.AssetControl)
                wsInitialized = true
            }
            return ws
        }

        private var wsMainInitialized = false
        private lateinit var wsMant: Webservice

        fun getMainWebservice(): Webservice {
            if (!wsMainInitialized) {
                wsMant = Webservice(WebServiceType.AssetControlMaintenance)
                wsMainInitialized = true
            }
            return wsMant
        }
    }

    enum class WebServiceType(val id: Long) {
        AssetControl(1), AssetControlMaintenance(2), ImageControl(3), Test(4)
    }

    var namespace = ""
    var url = ""
    var proxyUrl = ""
    var useProxy = false
    var proxyPort = 0
    var proxyUser = ""
    var proxyPass = ""

    private fun confWebservice() {
        when (webServiceType) {
            WebServiceType.AssetControl -> {
                url = svm.wsUrl
                namespace = svm.wsNamespace
                proxyUrl = svm.wsProxy
                useProxy = svm.wsUseProxy
                proxyPort = svm.wsProxyPort
                proxyUser = svm.wsProxyUser
                proxyPass = svm.wsProxyPass
            }

            WebServiceType.AssetControlMaintenance -> {
                url = svm.acMantWsServer
                namespace = svm.wsMantNamespace
                proxyUrl = svm.wsMantProxy
                useProxy = svm.wsMantUseProxy
                proxyPort = svm.wsMantProxyPort
                proxyUser = svm.wsMantProxyUser
                proxyPass = svm.wsMantProxyPass
            }

            WebServiceType.ImageControl -> {
                url = svm.wsIcUrl
                namespace = svm.wsIcNamespace
                proxyUrl = svm.wsIcProxy
                useProxy = svm.wsIcUseProxy
                proxyPort = svm.wsIcProxyPort
                proxyUser = svm.wsIcProxyUser
                proxyPass = svm.wsIcProxyPass
            }

            WebServiceType.Test -> {
                url = svm.wsTestUrl
                namespace = svm.wsTestNamespace
                proxyUrl = svm.wsTestProxyUrl
                useProxy = svm.wsTestUseProxy
                proxyPort = svm.wsTestProxyPort
                proxyUser = svm.wsTestProxyUser
                proxyPass = svm.wsTestProxyPass
            }

            else -> {}
        }
    }

    fun s(methodName: String): Any? {
        return s(methodName, null, null, null)
    }

    fun s(
        methodName: String,
        params: Array<WsParam>?,
    ): Any? {
        return s(methodName, params, null, null)
    }

    fun s(
        methodName: String,
        soapObjParams1: SoapObject?,
    ): Any? {
        return s(methodName, null, soapObjParams1, null)
    }

    fun s(
        methodName: String,
        soapObjParams1: SoapObject?,
        soapObjParams2: Array<SoapObject>?,
    ): Any? {
        return s(methodName, null, soapObjParams1, soapObjParams2)
    }

    fun s(
        methodName: String,
        soapObjParams: Array<SoapObject>?,
    ): Any? {
        return s(methodName, null, null, soapObjParams)
    }

    fun s(
        methodName: String,
        params: Array<WsParam>?,
        soapObjParams: Array<SoapObject>?,
    ): Any? {
        return s(methodName, params, null, soapObjParams)
    }

    fun s(
        methodName: String,
        params: Array<WsParam>?,
        soapObjParams1: SoapObject?,
    ): Any? {
        return s(methodName, params, soapObjParams1, null)
    }

    fun s(
        methodName: String,
        params: Array<WsParam>?,
        soapObjParams1: SoapObject?,
        soapObjParams2: Array<SoapObject>?,
    ): Any? {
        return s(methodName, params, soapObjParams1, soapObjParams2, false)
    }

    fun s(
        methodName: String,
        params: Array<WsParam>?,
        soapObjParams1: SoapObject?,
        soapObjParams2: Array<SoapObject>?,
        useConfSession: Boolean,
    ): Any? {
        // Configure WS
        confWebservice()

        val soapAction = "$url/$methodName"
        val soapObject = SoapObject(namespace, methodName)

        val sessionSoapObject: SoapObject = when (webServiceType) {
            WebServiceType.AssetControl,
            WebServiceType.AssetControlMaintenance,
                -> getSessionObject(useConfSession)

            else -> return null
        } ?: return null

        soapObject.addSoapObject(sessionSoapObject)

        if (params != null) for (item in params) {
            soapObject.addProperty(
                item.paramName, item.paramValue.toString()
            )
        }

        if (soapObjParams1 != null) {
            soapObject.addSoapObject(soapObjParams1)
        }

        if (soapObjParams2 != null) {
            for (soapObj in soapObjParams2) {
                soapObject.addSoapObject(soapObj)
            }
        }

        val response = getResponse(soapObject, soapAction) ?: return null

        if (webServiceType == WebServiceType.AssetControl || webServiceType == WebServiceType.AssetControlMaintenance) {
            val respVector = response as Vector<*>

            // El último Array del vector siempre es un ResponseObject
            val respObj =
                ResponseObject().getBySoapObject(respVector[respVector.size - 1] as SoapObject)

            if (respObj.resultCode == 0) {
                // Elimino el ResponseObject y devuelvo el resto como resultado de la consulta
                response.remove(response[response.size - 1])

                return when {
                    response[0] is Int -> response[0] as Int
                    response[0] is String -> response[0] as String
                    response[0] is Boolean -> response[0] as Boolean
                    else -> response[0]
                }
            } else {
                Log.e(this::class.java.simpleName, "$methodName: ${respObj.message}")
            }
        } else if (webServiceType == WebServiceType.ImageControl) {
            return response
        }
        return null
    }

    private fun getSessionObject(useConfSession: Boolean): SoapObject? {
        val sessionSoapObject = SoapObject(namespace, "session_obj")

        if (!useConfSession) {
            // No hay una sesión válida
            if (Statics.currentSession == null) {
                return null
            }

            // Objeto de Validación de Sesión
            sessionSoapObject.addProperty(
                "session_id", (Statics.currentSession ?: return null).sessionId
            )
            sessionSoapObject.addProperty("user_id", (Statics.currentSession ?: return null).userId)
        } else {
            // Objeto de sesión de configuración
            sessionSoapObject.addProperty("session_id", Md5.getMd5("dacosyssession"))
            sessionSoapObject.addProperty("user_id", 0)
        }

        return sessionSoapObject
    }

    @Throws(Exception::class)
    fun mysqlDate(): String {
        // Configure WS
        confWebservice()

        val methodName = "GetMysqlDate"
        val soapAction = "$url/$methodName"
        val soapObject = SoapObject(namespace, methodName)

        val response = getResponse(soapObject, soapAction)
        return response?.toString().orEmpty()
    }

    @Throws(Exception::class)
    fun addSession(
        userId: Long,
        password: String,
        userIp: String,
        userMacAddress: String,
        operatingSystem: String,
        appName: String,
        processorId: String,
        pcName: String,
        pcUserName: String,
    ): String {
        // Configure WS
        confWebservice()

        /*
            user_id: xsd:int
            password: xsd:string
            user_ip: xsd:string
            user_mac_address: xsd:string
            operating_system: xsd:string
            app_name: xsd:string
            processor_id: xsd:string
            pc_name: xsd:string
            pc_user_name: xsd:string
        */

        val methodName = "Session_Add"
        val params = arrayOf(
            WsParam("user_id", userId),
            WsParam("password", password),
            WsParam("user_ip", userIp),
            WsParam("user_mac_address", userMacAddress),
            WsParam("operating_system", operatingSystem),
            WsParam("app_name", appName),
            WsParam("processor_id", processorId),
            WsParam("pc_name", pcName),
            WsParam("pc_user_name", pcUserName)
        )

        val soapAction = "$url/$methodName"
        val soapObject = SoapObject(namespace, methodName)

        for (item in params) {
            soapObject.addProperty(item.paramName, item.paramValue.toString())
        }

        val response = getResponse(soapObject, soapAction) ?: return ""

        val respVector = response as Vector<*>

        // El último Array del vector siempre es un ResponseObject
        val respObj =
            ResponseObject().getBySoapObject(respVector[respVector.size - 1] as SoapObject)

        if (respObj.resultCode == 0) {
            // Elimino el ResponseObject y devuelvo el resto como resultado de la consulta
            respVector.remove(respVector[respVector.size - 1])
            return respVector.toString().replace("[", "").replace("]", "")
        } else {
            Log.e(this::class.java.simpleName, "$methodName: ${respObj.message}")
        }
        return ""
    }

    private fun getResponse(soapObject: SoapObject, soapAction: String): Any? {
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = true
        envelope.implicitTypes = true
        envelope.encodingStyle = SoapSerializationEnvelope.ENC
        envelope.setOutputSoapObject(soapObject)

        // Permite convertir correctamente los valores tipo float, decimal o double
        // en los parámetros que se están pasando. Evita un CastException.
        MarshalFloat().register(envelope)

        /////////////////////////////////////
        // Destripar el URL del webservice //
        val wsProtocol: String
        val wsDomain: String
        val wsPort: Int
        val wsUri: String
        val wsQuery: String

        try {
            val pattern = Pattern.compile("(https?://)([^:^/]*)(:\\d*)?(.*)?")
            val matcher = pattern.matcher(url)
            matcher.find()

            /*
            https://dev.example.com:443/Milestone13/ac/s1/service.php

            protocol: http://
            domain: dev.example.com
            port: :443
            uri: /Milestone13/ac/s1/service.php
            */

            wsProtocol = matcher.group(1) ?: ""
            wsDomain = matcher.group(2) ?: ""

            wsPort = when (wsProtocol) {
                "https://" -> 443
                else -> 80
            }

            /*
            // Descartamos el puerto
            val x = matcher.group(3)
            if (x != null) {
                wsPort = x.replace(":", "")
            }
            */

            wsUri = matcher.group(4) ?: ""
            wsQuery = "?wsdl"
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        var proxy = Proxy.NO_PROXY
        if (useProxy) {
            val authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        proxyUser, proxyPass.toCharArray()
                    )
                }
            }
            Authenticator.setDefault(authenticator)

            // Usar el proxy predeterminado
            if (proxyUrl.isEmpty()) {
                val proxyHost = System.getProperty("http.proxyHost")
                if (proxyHost != null) {
                    proxyUrl = proxyHost
                }

                val portStr = System.getProperty("http.proxyPort")
                if (portStr != null) {
                    proxyPort = Integer.parseInt(portStr)
                }
            }

            if (proxyUrl.isNotEmpty() && proxyPort > 0) {
                proxy = Proxy(
                    Proxy.Type.HTTP, InetSocketAddress(
                        proxyUrl, proxyPort
                    )
                )
            }
        }

        // Respuesta del webservice
        val response: Any

        try {
            val timeout = sr.prefsGetInt(Preference.connectionTimeout) * 1000
            val headers: MutableList<HeaderProperty> = ArrayList()
            headers.add(HeaderProperty("Connection", "close"))

            when (wsProtocol) {
                "https://" -> {
                    val aht = HttpsTransportSE(
                        proxy, wsDomain, wsPort, "$wsUri$wsQuery", timeout
                    )
                    aht.debug = false

                    val sslContext = CustomSSLContext.createCustomSSLContext()
                    (aht.serviceConnection as HttpsServiceConnectionSE).setSSLSocketFactory(sslContext.socketFactory)

                    callWithRetry(aht, soapAction, envelope, headers)
                    response = (envelope.response ?: return null)
                }

                "http://" -> {
                    val aht = HttpTransportSE(
                        proxy, "$url$wsQuery", timeout
                    )
                    aht.debug = false

                    callWithRetry(aht, soapAction, envelope, headers)
                    response = (envelope.response ?: return null)
                }

                else -> return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return response
    }

    @Suppress("unused")
    @Throws(IOException::class, XmlPullParserException::class)
    fun call(
        aht: HttpTransportSE,
        soapAction: String,
        envelope: SoapEnvelope,
        headers: List<*>,
    ): List<*>? {
        return callWithRetry(aht, soapAction, envelope, headers, 1)
    }

    @Suppress("unused")
    @Throws(IOException::class, XmlPullParserException::class)
    fun callWithRetry(
        aht: HttpTransportSE,
        soapAction: String,
        envelope: SoapEnvelope,
        headers: List<*>,
        numRetries: Int = 3,
    ): List<*>? {
        var attempt = 0
        while (attempt < numRetries) {
            if (attempt > 0) {
                Thread.sleep(3000)
                aht.serviceConnection.connect()
            }

            try {
                Log.d(
                    this::class.java.simpleName, String.format(
                        "%s: %s", soapAction, envelope.bodyOut?.toString().orEmpty()
                    )
                )

                val r = aht.call(soapAction, envelope, headers, null)
                aht.serviceConnection.disconnect()
                return r
            } catch (e: Exception) {
                e.printStackTrace()

                attempt++
                if (attempt >= numRetries) {
                    Log.e(
                        this::class.java.simpleName,
                        "Error al conectar después de $numRetries intentos"
                    )
                    throw e
                } else {
                    Log.i(this::class.java.simpleName, "Reiniciado conexión...")
                    try {
                        aht.reset()
                        aht.serviceConnection.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null
    }
}