package com.dacosys.assetControl.utils.settings.devices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.util.AttributeSet
import android.util.Log
import androidx.preference.ListPreference
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.utils.Statics.Companion.appHasBluetoothPermission

/**
 * Created by Agustin on 16/01/2017.
 */

@SuppressLint("MissingPermission")
class DevicePreference
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ListPreference(context, attrs) {

    private val context = AssetControlApp.getContext()

    init {
        entries = entries()
        entryValues = entryValues()

        if (entries != null && entries.isNotEmpty() &&
            entryValues != null && entryValues.isNotEmpty()
        ) {
            setValueIndex(initializeIndex())
        }
    }

    private fun entries(): Array<CharSequence> {
        val allDescription: ArrayList<String> = ArrayList()
        try {
            //action to provide entry data in char sequence array for list
            val allDevices = getAll()
            if (appHasBluetoothPermission()) {
                allDescription.addAll(allDevices.indices.map { allDevices[it].name })
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return allDescription.toTypedArray()
    }

    private fun entryValues(): Array<CharSequence> {
        val allValues: ArrayList<String> = ArrayList()
        try {
            //action to provide value data for list
            val allDevices = getAll()
            allValues.addAll(allDevices.indices.map { allDevices[it].address })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return allValues.toTypedArray()
    }

    private fun initializeIndex(): Int {
        //here you can provide the value to set (typically retrieved from the SharedPreferences)
        //...
        return 0
    }

    private fun getAll(): ArrayList<BluetoothDevice> {
        val devices: ArrayList<BluetoothDevice> = ArrayList()
        try {
            val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val mBluetoothAdapter = bluetoothManager.adapter

            if (appHasBluetoothPermission()) {
                val pairedDevices = mBluetoothAdapter?.bondedDevices
                if (pairedDevices != null && pairedDevices.isNotEmpty()) {
                    for (device in pairedDevices) {
                        devices.add(device)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e(this::class.java.simpleName, ex.message ?: "")
        }

        return devices
    }
}