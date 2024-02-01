package com.dacosys.assetControl.utils.printer

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.settings.Preference
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException

open class NetPrinter(private val onEvent: (SnackBarEventData) -> Unit) :
    Printer.PrintLabelListener {

    private val tag = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName

    override fun printLabel(printThis: String, qty: Int, onFinish: (Boolean) -> Unit) {
        // Impresora guardada en preferencias
        val ipPrinter = Preferences.prefsGetString(Preference.ipNetPrinter)
        val portPrinter = Preferences.prefsGetInt(Preference.portNetPrinter)

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
                        "${getContext().getString(R.string.unknown_host)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                } catch (e: ConnectException) {
                    e.printStackTrace()
                    sendEvent(
                        "${getContext().getString(R.string.error_connecting_to)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                } catch (e: IOException) {
                    e.printStackTrace()
                    sendEvent(
                        "${getContext().getString(R.string.error_printing_to)} $ipPrinter ($portPrinter)",
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