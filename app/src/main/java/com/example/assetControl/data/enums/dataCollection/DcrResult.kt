package com.example.assetControl.data.enums.dataCollection

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class DcrResult(val id: Int, val description: String) {
    companion object {
        var cont = DcrResult(0, context.getString(R.string._continue))
        var back = DcrResult(-1, context.getString(R.string.back))
        var end = DcrResult(-2, context.getString(R.string.end))
        var noContinue = DcrResult(-3, context.getString(R.string.no_continue))
        var levelX = DcrResult(-4, context.getString(R.string.level_x))

        fun getAll(): List<DcrResult> {
            return listOf(
                cont,
                back,
                end,
                noContinue,
                levelX
            )
        }

        fun getById(id: Int): DcrResult? {
            return getAll().firstOrNull { it.id == id }
        }
    }
}


