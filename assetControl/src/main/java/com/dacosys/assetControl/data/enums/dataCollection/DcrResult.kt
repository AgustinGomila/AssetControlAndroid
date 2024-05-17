package com.dacosys.assetControl.data.enums.dataCollection

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class DcrResult(val id: Int, val description: String) {
    companion object {
        var cont = DcrResult(0, getContext().getString(R.string._continue))
        var back = DcrResult(-1, getContext().getString(R.string.back))
        var end = DcrResult(-2, getContext().getString(R.string.end))
        var noContinue = DcrResult(-3, getContext().getString(R.string.no_continue))
        var levelX = DcrResult(-4, getContext().getString(R.string.level_x))

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


