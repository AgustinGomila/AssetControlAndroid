package com.dacosys.assetControl.sync.functions

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.wsGeneral.SessionObject
import kotlinx.coroutines.*
import java.net.NetworkInterface
import java.util.*

class SetCurrentSession {
    var mCallback: Statics.SessionCreated? = null

    fun addParams(callback: Statics.SessionCreated) {
        this.mCallback = callback
    }

    fun execute(): Boolean {
        val result = doInBackground()
        return postExecute(result)
    }

    private fun postExecute(result: Boolean): Boolean {
        mCallback?.onSessionCreated(result)
        return result
    }

    private var deferred: Deferred<Boolean>? = null

    private fun doInBackground(): Boolean {
        if (Statics.currentUserId == null) {
            Statics.currentSession = null
            return false
        }

        var result = false
        runBlocking {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        return result
    }

    @SuppressLint("HardwareIds")
    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        val ip = getIPAddress()
        if (ip.isEmpty()) {
            Statics.currentSession = null
            return@withContext false
        }

        val macAddress = getMACAddress()

        val user = Statics.currentUser()
        if (user == null) {
            Statics.currentSession = null
            return@withContext false
        }

        val operatingSystem =
            "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

        val pm = Statics.AssetControl.getContext().packageManager
        val pInfo = pm.getPackageInfo(Statics.AssetControl.getContext().packageName, 0)
        var appName = "Unknown"
        if (pInfo != null) {
            appName = "${pm.getApplicationLabel(pInfo.applicationInfo)} ${
                Statics.AssetControl.getContext().getString(R.string.app_milestone)
            } ${pInfo.versionName}"
        }

        val pcUserName = "Unknown"
        val processorId = Settings.Secure.getString(
            Statics.AssetControl.getContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val pcName = "${Build.MANUFACTURER} ${Build.MODEL}"

        /*
            userId: Long,
            password: String,
            userIp: String,
            userMacAddress: String,
            operatingSystem: String,
            appName: String,
            processorId: String,
            pcName: String,
            pcUserName:String
        */

        val sessionId = Statics.getWebservice().addSession(
            userId = user.userId,
            password = user.password,
            userIp = ip,
            userMacAddress = macAddress,
            operatingSystem = operatingSystem,
            appName = appName,
            processorId = processorId,
            pcName = pcName,
            pcUserName = pcUserName
        )

        if (sessionId.isNotEmpty()) {
            val cs = SessionObject()
            cs.sessionId = sessionId
            cs.userId = Statics.currentUserId!!

            Statics.currentSession = cs
        } else {
            Statics.currentSession = null
            return@withContext false
        }

        return@withContext Statics.currentSession != null
    }

    private fun getMACAddress(interfaceName: String = "wlan0"): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intF in interfaces) {
                if (!intF.name.equals(interfaceName, true)) continue
                val mac = intF.hardwareAddress ?: return ""
                val buf = StringBuilder()
                for (aMac in mac) buf.append(String.format("%02X:", aMac))
                if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
                return buf.toString().replace(":", "")
            }
        } catch (ignored: Exception) {
        }

        // for now eat exceptions
        return ""
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    private fun getIPAddress(useIPv4: Boolean = true): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        // for now eat exceptions
        return ""
    }
}