package com.dacosys.assetControl.devices.printer

import androidx.fragment.app.FragmentActivity
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences

object Printer {
    interface PrintLabelListener {
        fun printLabel(printThis: String, qty: Int, onFinish: (Boolean) -> Unit)
    }

    object PrinterFactory {
        fun createPrinter(activity: FragmentActivity, onEvent: (SnackBarEventData) -> Unit): PrintLabelListener? {
            // Impresora guardada en preferencias
            val useBtPrinter = Preferences.prefsGetBoolean(Preference.useBtPrinter)
            val useNetPrinter = Preferences.prefsGetBoolean(Preference.useNetPrinter)

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