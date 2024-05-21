package com.dacosys.assetControl.ui.adapters.user

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.R.id.descriptionTextView
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.data.room.dto.user.User

/**
 * Created by Agustin on 18/01/2017.
 */

class UserAdapter : ArrayAdapter<User> {

    constructor(resource: Int) : super(AssetControlApp.getContext(), resource)

    constructor(resource: Int, user: List<User>, spinner: Spinner?) : super(
        AssetControlApp.getContext(),
        resource,
        user
    ) {
        this.spinner = spinner
        this.resource = resource
    }

    private var spinner: Spinner? = null
    private var resource: Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)
        }

        if (position >= 0) {
            val user = getItem(position)

            if (user != null) {
                // Font colors
                val dimgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.dimgray,
                        null
                    )
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_dark,
                        null
                    )

                if (resource == custom_spinner_dropdown_item && v != null) {
                    val tt1 = v.findViewById<TextView>(descriptionTextView)

                    if (tt1 != null) {
                        tt1.text = user.name
                        if (user.id == 0L) {
                            tt1.setTextColor(dimgray)
                        } else {
                            tt1.setTextColor(black)
                        }
                    }
                }
            }
        }
        if ((v?.height ?: 0) > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v?.height}}-------")
        }
        return v!!
    }
}