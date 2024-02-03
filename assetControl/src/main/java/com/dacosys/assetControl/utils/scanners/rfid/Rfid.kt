package com.dacosys.assetControl.utils.scanners.rfid

import android.util.Log
import com.dacosys.assetControl.utils.scanners.vh75.Utility
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences

/**
 * Created by Agustin on 19/10/2017.
 */

open class Rfid {
    interface RfidDeviceListener {
        fun onStateChanged(state: Int) {}

        fun onRead(data: ByteArray, bytes: Int) {
            val res = data.copyOfRange(0, bytes)

            Log.v(
                this::class.java.simpleName,
                "onRead Data (b: $bytes): " + Utility.bytes2HexStringWithSeparator(res)
            )
            if (rfidDevice != null) {
                if (rfidDevice is Vh75Bt) {
                    Vh75Bt.processMessage(this, res, bytes)
                }
            }
        }

        fun onWrite(data: ByteArray) {
            Log.v(
                this::class.java.simpleName,
                "onWrite Data: " + Utility.bytes2HexStringWithSeparator(data)
            )
        }

        fun onDeviceName(deviceName: String) {
            Log.v(this::class.java.simpleName, "onDeviceName deviceName: $deviceName")
        }

        fun onReadCompleted(scanCode: String)
        fun onWriteCompleted(isOk: Boolean)
        fun onGetBluetoothName(name: String)
    }

    companion object {
        // region Public methods
        var rfidDevice: Rfid? = null

        private fun initRfidRequired(): Boolean {
            return if (isRfidRequired()) {
                if (rfidDevice == null) {
                    true
                } else {
                    if ((rfidDevice is Vh75Bt)) {
                        (rfidDevice as Vh75Bt).mState == Vh75Bt.STATE_NONE
                    } else false
                }
            } else false
        }

        fun isRfidRequired(): Boolean {
            if (!Preferences.prefsGetBoolean(Preference.useBtRfid)) {
                return false
            }

            val btAddress = Preferences.prefsGetString(Preference.rfidBtAddress)
            return btAddress.isNotEmpty()
        }

        fun resume(listener: RfidDeviceListener) {
            if (rfidDevice != null) {
                if (rfidDevice is Vh75Bt) {
                    Log.v(this::class.java.simpleName, "RFID Listener: $listener")
                    (rfidDevice as Vh75Bt).setListener(null)
                    (rfidDevice as Vh75Bt).resume()
                    (rfidDevice as Vh75Bt).setListener(listener)
                }
            }
        }

        fun pause() {
            if (rfidDevice != null) {
                if (rfidDevice is Vh75Bt) {
                    Log.v(this::class.java.simpleName, "RFID Listener: NULL")
                    (rfidDevice as Vh75Bt).setListener(null)
                    (rfidDevice as Vh75Bt).pause()
                }
            }
        }

        fun startScan() {
            if (rfidDevice != null) {
            }
        }

        fun stopScan() {
            if (rfidDevice != null) {
            }
        }

        fun destroy() {
            if (rfidDevice != null) {
                if (rfidDevice is Vh75Bt) {
                    (rfidDevice as Vh75Bt).setListener(null)
                    (rfidDevice as Vh75Bt).destroy()
                }
                rfidDevice = null
            }
        }

        fun lockScanner(lock: Boolean) {
            if (rfidDevice != null) {
            }
        }

        fun getStatus(): Int {
            if (rfidDevice != null) {
            }
            return -1
        }
        //endregion

        fun setListener(listener: RfidDeviceListener, rfidType: RfidType) {
            if (initRfidRequired()) {
                build(listener, rfidType)
            } else {
                if (rfidDevice != null && rfidDevice is Vh75Bt) {
                    (rfidDevice as Vh75Bt).setListener(listener)
                }
            }
        }

        //endregion
        fun build(listener: RfidDeviceListener?, rfidType: RfidType): Rfid? {
            if (rfidType == RfidType.vh75) {
                rfidDevice = Vh75Bt(listener)
            }
            return rfidDevice
        }
    }
}