package com.example.assetControl.devices.scanners.honeywell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.assetControl.R

@Suppress("unused")
class HoneywellBroadcastReceiver : BroadcastReceiver {
    private val tag = this::class.java.simpleName
    private lateinit var honeywell: Honeywell

    constructor()

    constructor(honeywellBroadcasts: Honeywell) : super() {
        this.honeywell = honeywellBroadcasts
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.w(tag, "Intent received {$intent} ({$context})")

        fun bytesToHexString(arr: ByteArray?): String {
            var s = "[]"
            if (arr != null) {
                s = "["
                for (i in arr.indices) {
                    s += "0x" + Integer.toHexString(arr[i].toInt()) + ", "
                }
                s = s.substring(0, s.length - 2) + "]"
            }
            return s
        }

        try {
            when (intent.action) {
                Honeywell.Constants.ACTION_BARCODE_DATA -> {
                    if (honeywell.lockScannerEvent) return

                    /*
                    These extras are available:
                        "version" (int) = Data Intent Api version
                        "aimId" (String) = The AIM Identifier
                        "charset" (String) = The charset used to convert "dataBytes" to "data" string "codeId" (String) = The Honeywell Symbology Identifier
                        "data" (String) = The barcode data as a string
                        "dataBytes" (byte[]) = The barcode data as a byte array
                        "timestamp" (String) = The barcode timestamp
                    */

                    val version = intent.getIntExtra("version", 0)
                    val aimId = intent.getStringExtra("aimId")
                    val charset = intent.getStringExtra("charset")
                    val codeId = intent.getStringExtra("codeId")
                    val data = intent.getStringExtra("data")
                    val dataBytes = intent.getByteArrayExtra("dataBytes")
                    val dataBytesStr: String = bytesToHexString(dataBytes)
                    val timestamp = intent.getStringExtra("timestamp")

                    val text = String.format(
                        """
                            Version: %s
                            Data: %s
                            Charset: %s
                            Bytes: %s
                            AimId: %s
                            CodeId: %s
                            Timestamp: %s
                        """.trimIndent(),
                        version, data, charset, dataBytesStr, aimId, codeId, timestamp
                    )
                    Log.i(tag, text)

                    if (version >= 1) {
                        if (data != null) {
                            honeywell.sendScannedData(data)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(
                tag,
                context.getString(R.string.barcode_failure)
            )
        }
    }
}