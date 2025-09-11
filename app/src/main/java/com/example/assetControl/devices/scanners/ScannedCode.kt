package com.example.assetControl.devices.scanners

import android.util.Log
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository

class ScannedCode {
    private var targetInt: Any? = null
    private var targetObject: Any? = null

    constructor(warehouseArea: WarehouseArea) : this(
        warehouseArea as Any
    )

    constructor(asset: Asset, labelNbr: Int?) : this(
        asset as Any,
        labelNbr
    )

    constructor(targetObject: Any) {
        this.targetObject = targetObject
    }

    constructor(targetObject: Any, targetInt: Int?) {
        this.targetObject = targetObject
        this.targetInt = targetInt
    }

    constructor() {
        targetObject = null
    }

    val warehouseArea: WarehouseArea?
        get() = if (targetObject != null && targetObject!!.javaClass == WarehouseArea::class.java) {
            targetObject as WarehouseArea
        } else null

    val asset: Asset?
        get() = if (targetObject != null && targetObject!!.javaClass == Asset::class.java) {
            targetObject as Asset
        } else null

    val labelNbr: Int?
        get() = if (targetObject != null && targetObject!!.javaClass == Asset::class.java) {
            targetInt as Int?
        } else null

    val codeFound: Boolean
        get() {
            return targetObject != null
        }

    private fun searchString(origin: String, formula: String, position: Int): String {
        //  ex @"#WA#{0:00000}#"
        val rx = Regex(formula)
        val matches = rx.matchEntire(origin)

        if (matches != null) {
            if (matches.groups.size >= position && matches.groups[position].toString()
                    .isNotEmpty()
            ) {
                try {
                    return matches.groups[position]?.value.toString()
                } catch (ex: Exception) {
                    val res =
                        "Error doing regex.\r\n formula $formula\r\n string $origin\r\n${ex.message}"
                    Log.e(this::class.java.simpleName, res)
                }
            }
        }
        return ""
    }

    fun getFromCode(
        code: String,
        searchWarehouseAreaId: Boolean,
        searchAssetCode: Boolean,
        searchAssetSerial: Boolean,
        searchAssetEan: Boolean,
    ): ScannedCode {
        var currentCode = code
        if (searchWarehouseAreaId) {
            val match: String = searchString(currentCode, """#WA#(\d{5})#""", 1)
            if (match.isNotEmpty()) {
                return try {
                    val tempWarehouseArea = WarehouseAreaRepository().selectById(match.toLong())
                    if (tempWarehouseArea != null)
                        ScannedCode(tempWarehouseArea)
                    else ScannedCode()
                } catch (ex: Exception) {
                    ScannedCode()
                }
            }
        }

        if (searchAssetCode) {
            var labelNumber: Int? = null
            val formula = """(.+)#(\d+)$"""

            // search for the label nbr code
            val match = searchString(currentCode, formula, 2)

            if (match.isNotEmpty()) {
                try {
                    labelNumber = match.toInt()
                    currentCode = searchString(currentCode, formula, 1)
                } catch (ex: Exception) {
                    // labelNumber is not ok, so we set it to null
                    labelNumber = null
                    currentCode = code
                }
            }

            val assetArray = AssetRepository().selectByCode(currentCode)
            try {
                if (assetArray.isNotEmpty()) {
                    return ScannedCode(assetArray[0], labelNumber)
                }
            } catch (ex: Exception) {
                return ScannedCode()
            }
        }

        if (searchAssetSerial) {
            val assetArray = AssetRepository().selectBySerial(currentCode)
            try {
                if (assetArray.isNotEmpty()) {
                    return ScannedCode(assetArray[0])
                }
            } catch (ex: Exception) {
                return ScannedCode()
            }
        }

        if (searchAssetEan) {
            val assetArray = AssetRepository().selectByEan(currentCode)
            try {
                if (assetArray.isNotEmpty()) {
                    return ScannedCode(assetArray[0])
                }
            } catch (ex: Exception) {
                return ScannedCode()
            }
        }

        // return nothing if not found or if not validated
        return ScannedCode()
    }
}