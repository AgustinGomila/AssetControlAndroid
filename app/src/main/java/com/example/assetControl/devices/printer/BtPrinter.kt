package com.example.assetControl.devices.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.utils.Statics.Companion.appHasBluetoothPermission
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences
import com.google.android.gms.common.api.CommonStatusCodes
import java.io.IOException

@SuppressLint("MissingPermission")
open class BtPrinter(private val activity: FragmentActivity, private val onEvent: (SnackBarEventData) -> Unit) :
    Printer.PrintLabelListener, Runnable {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null

    private var bluetoothDevice: BluetoothDevice? = null
        get() {
            if (field == null) {
                refreshBluetoothPrinter()
            }
            return field
        }

    private val tag: String = this::class.java.simpleName

    private fun initializePrinter() {
        val printer = bluetoothDevice ?: return

        val bluetoothManager =
            context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager

        val mBluetoothAdapter = bluetoothManager.adapter

        if (mBluetoothAdapter == null) {
            sendEvent(context.getString(R.string.there_are_no_bluetooth_devices), SnackBarType.ERROR)
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                if (!appHasBluetoothPermission()) {
                    resultForPrinterBtPermissions()
                } else {
                    enableBluetooth()
                }
            } else {
                connectToPrinter(printer.address)
            }
        }
    }

    private fun enableBluetooth() {
        val enablePrinter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enablePrinter.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        resultForPrinterConnect.launch(enablePrinter)
    }

    private fun connectToPrinter(deviceAddress: String) {
        Log.v(tag, "Coming incoming address $deviceAddress")
        bluetoothDevice = bluetoothAdapter?.getRemoteDevice(deviceAddress)

        bluetoothSocket?.close()

        val mBluetoothConnectThread = Thread(this)
        mBluetoothConnectThread.start()
    }

    override fun run() {
        try {
            if (appHasBluetoothPermission()) {
                val device = bluetoothDevice ?: return

                bluetoothSocket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()

                sendEvent(activity.getString(R.string.device_connected), SnackBarType.INFO)
            }
        } catch (eConnectException: IOException) {
            Log.e(tag, "CouldNotConnectToSocket", eConnectException)
            bluetoothSocket?.close()

            sendEvent(context.getString(R.string.error_connecting_device), SnackBarType.ERROR)
            return
        }
    }

    private fun resultForPrinterBtPermissions() {
        if (!appHasBluetoothPermission()) {
            requestPermissionsPrinter.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            enableBluetooth()
        }
    }

    private val requestPermissionsPrinter = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            enableBluetooth()
        }
    }

    private val resultForPrinterConnect =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it?.resultCode == CommonStatusCodes.SUCCESS || it?.resultCode == CommonStatusCodes.SUCCESS_CACHE) {
                val device = bluetoothDevice ?: return@registerForActivityResult
                connectToPrinter(device.address)
            }
        }

    private fun sendEvent(message: String, type: SnackBarType) {
        onEvent(SnackBarEventData(message, type))
    }

    override fun printLabel(printThis: String, qty: Int, onFinish: (Boolean) -> Unit) {
        val t = object : Thread() {
            override fun run() {
                try {
                    val socket = bluetoothSocket
                    if (socket != null) {
                        val os = socket.outputStream
                        for (i in 0 until qty) {
                            os.write(printThis.toByteArray())
                        }
                        os.flush()
                        onFinish(true)
                    } else {
                        onFinish(false)
                    }
                } catch (e: Exception) {
                    ErrorLog.writeLog(
                        activity,
                        tag,
                        "${context.getString(R.string.exception_error)}: " + e.message
                    )

                    val printerBtAddress = Preferences.prefsGetString(Preference.printerBtAddress)
                    sendEvent(
                        "${context.getString(R.string.error_connecting_to)}: $printerBtAddress",
                        SnackBarType.ERROR
                    )
                    onFinish(false)
                }
            }
        }
        t.start()
    }

    private fun refreshBluetoothPrinter() {
        // Impresora guardada en preferencias
        val useBtPrinter = Preferences.prefsGetBoolean(Preference.useBtPrinter)
        val printerBtAddress = Preferences.prefsGetString(Preference.printerBtAddress)

        if (useBtPrinter) {
            if (printerBtAddress.isEmpty()) {
                onEvent(
                    SnackBarEventData(
                        context.getString(R.string.bluetooth_printer_not_configured),
                        SnackBarType.ERROR
                    )
                )
                return
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter ?: return

            if (!appHasBluetoothPermission()) {
                onEvent(SnackBarEventData(context.getString(R.string.permission_denied), SnackBarType.ERROR))
                return
            }

            val mPairedDevices = bluetoothAdapter.bondedDevices

            this.bluetoothAdapter = bluetoothAdapter

            if (mPairedDevices.isNotEmpty()) {
                for (mDevice in mPairedDevices) {
                    if (mDevice.address == printerBtAddress) {
                        bluetoothDevice = mDevice
                        return
                    }
                }
            }

            onEvent(
                SnackBarEventData(
                    context.getString(R.string.there_are_no_bluetooth_devices),
                    SnackBarType.ERROR
                )
            )
        }
    }

    init {
        initializePrinter()
    }
}