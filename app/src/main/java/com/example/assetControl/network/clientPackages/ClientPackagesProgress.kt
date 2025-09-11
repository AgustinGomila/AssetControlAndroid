package com.example.assetControl.network.clientPackages

import com.example.assetControl.network.utils.ProgressStatus
import org.json.JSONObject

data class ClientPackagesProgress(
    var status: ProgressStatus = ProgressStatus.unknown,
    var result: ArrayList<JSONObject> = ArrayList(),
    var clientEmail: String = "",
    var clientPassword: String = "",
    var msg: String = "",
)