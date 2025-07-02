package com.dacosys.assetControl.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.utils.Statics.Companion.DEMO_AUTO_SEND
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences

class Connection {
    companion object {
        fun autoSend(): Boolean {
            return isOnline() && (Preferences.prefsGetBoolean(Preference.autoSend) || DEMO_AUTO_SEND)
        }

        @SuppressLint("MissingPermission")
        fun isOnline(): Boolean {
            val connectivityManager =
                AssetControlApp.context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    //for another device how are able to connect with Ethernet
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                    //check the internet over Bluetooth
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_BLUETOOTH")
                        return true
                    }
                }
            }
            Log.w("Internet", "NO CONNECTION")
            return false
        }
    }
}