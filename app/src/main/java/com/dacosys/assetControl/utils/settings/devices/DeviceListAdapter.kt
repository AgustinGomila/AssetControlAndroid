package com.dacosys.assetControl.utils.settings.devices

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ListView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha

class DeviceListAdapter(
    private var resource: Int,
    device: List<PrinterDevice>,
    private var listView: ListView?,
) : ArrayAdapter<DeviceListAdapter.Companion.PrinterDevice>(
    context,
    resource,
    device
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(R.layout.device_row, parent, false)
        }

        if (position >= 0) {
            val device = getItem(position)

            if (device != null) {
                val tt1 = v!!.findViewById<CheckedTextView>(R.id.mac_address)
                val tt2 = v.findViewById<CheckedTextView>(R.id.name)

                if (tt1 != null) {
                    tt1.text = device.mac_address
                }

                if (tt2 != null) {
                    tt2.text = device.name
                }

                // Background colors
                val whitesmoke =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.whitesmoke,
                        null
                    )

                // Font colors
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.text_dark,
                        null
                    )

                v.setBackgroundColor(whitesmoke)
                tt1.setTextColor(black)
                tt2.setTextColor(black)
            }

            if (listView!!.isItemChecked(position)) {
                v!!.background.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColorWithAlpha(
                            colorId = R.color.lightslategray,
                            alpha = 240
                        ),
                        BlendModeCompat.MODULATE
                    )
            } else {
                v!!.background.colorFilter = null
            }
        }
        if ((v?.height ?: 0) > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v?.height}}-------")
        }
        return v!!
    }

    companion object {
        class PrinterDevice(var mac_address: String, var name: String) : Parcelable {
            constructor(parcel: Parcel) : this(parcel.readString().orEmpty(), parcel.readString().orEmpty())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(mac_address)
                parcel.writeString(name)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<PrinterDevice> {
                override fun createFromParcel(parcel: Parcel): PrinterDevice {
                    return PrinterDevice(parcel)
                }

                override fun newArray(size: Int): Array<PrinterDevice?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}