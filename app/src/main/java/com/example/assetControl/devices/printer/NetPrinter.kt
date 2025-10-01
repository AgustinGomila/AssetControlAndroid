package com.example.assetControl.devices.printer

import android.util.Log
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.R
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.utils.settings.config.Preference
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException

open class NetPrinter(private val onEvent: (SnackBarEventData) -> Unit) :
    Printer.PrintLabelListener {

    private val tag = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName

    override fun printLabel(printThis: String, qty: Int, onFinish: (Boolean) -> Unit) {
        // Impresora guardada en preferencias
        val ipPrinter = sr.prefsGetString(Preference.ipNetPrinter)
        val portPrinter = sr.prefsGetInt(Preference.portNetPrinter)

        Log.v(tag, "Printer IP: $ipPrinter ($portPrinter)")
        Log.v(tag, printThis)

        val t = object : Thread() {
            override fun run() {
                try {
                    val socket = Socket(ipPrinter, portPrinter)
                    val os = socket.outputStream
                    for (i in 0 until qty) {
                        os.write(printThis.toByteArray())
                    }
                    os.flush()
                    socket.close()
                    onFinish(true)
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                    sendEvent(
                        "${context.getString(R.string.unknown_host)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                } catch (e: ConnectException) {
                    e.printStackTrace()
                    sendEvent(
                        "${context.getString(R.string.error_connecting_to)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                } catch (e: IOException) {
                    e.printStackTrace()
                    sendEvent(
                        "${context.getString(R.string.error_printing_to)} $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                }
            }
        }
        t.start()
    }

    private fun sendEvent(message: String, type: SnackBarType) {
        onEvent(SnackBarEventData(message, type))
    }
}