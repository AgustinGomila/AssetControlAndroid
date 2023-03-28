package com.dacosys.assetControl.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.utils.settings.Preference

class Printer {
    companion object {
        var printerBluetoothDevice: BluetoothDevice? = null
            get() {
                if (field == null) {
                    refreshBluetoothPrinter()
                }
                return field
            }

        @SuppressLint("MissingPermission")
        private fun refreshBluetoothPrinter() {
            if (Preferences.prefsGetBoolean(Preference.useBtPrinter)) {
                val printerMacAddress = Preferences.prefsGetString(Preference.printerBtAddress)
                if (printerMacAddress.isEmpty()) {
                    return
                }

                val bluetoothManager = AssetControlApp.getContext()
                    .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val mBluetoothAdapter = bluetoothManager.adapter

                if (ActivityCompat.checkSelfPermission(
                        AssetControlApp.getContext(), Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                val mPairedDevices = mBluetoothAdapter!!.bondedDevices

                if (mPairedDevices.size > 0) {
                    for (mDevice in mPairedDevices) {
                        if (mDevice.address == printerMacAddress) {
                            printerBluetoothDevice = mDevice
                            return
                        }
                    }
                }
            }
        }
    }
}