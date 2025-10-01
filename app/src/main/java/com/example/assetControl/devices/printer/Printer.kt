package com.example.assetControl.devices.printer

import androidx.fragment.app.FragmentActivity
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.R
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.utils.settings.config.Preference

object Printer {
    interface PrintLabelListener {
        fun printLabel(printThis: String, qty: Int, onFinish: (Boolean) -> Unit)
    }

    object PrinterFactory {
        fun createPrinter(activity: FragmentActivity, onEvent: (SnackBarEventData) -> Unit): PrintLabelListener? {
            // Impresora guardada en preferencias
            val useBtPrinter = sr.prefsGetBoolean(Preference.useBtPrinter)
            val useNetPrinter = sr.prefsGetBoolean(Preference.useNetPrinter)

            return when {
                useNetPrinter -> NetPrinter(onEvent)
                useBtPrinter -> BtPrinter(activity, onEvent)

                else -> {
                    val msg = context.getString(R.string.there_is_no_selected_printer)
                    onEvent(SnackBarEventData(msg, SnackBarType.ERROR))
                    null
                }
            }
        }
    }
}