package com.dacosys.assetControl.data.model.barcode.fields

import com.dacosys.assetControl.data.model.barcode.BarcodeLabelPrintOps
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.settings.entries.ConfEntry

class BarcodeLabel(templateStr: String) {
    private var templateStr: String = ""
    private var printOps: BarcodeLabelPrintOps? = null
    var barcodeFields: ArrayList<BarcodeLabelField>? = null

    init {
        this.templateStr = templateStr

        printOps = BarcodeLabelPrintOps(
            prefsGetInt(ConfEntry.prnPrinterSpeed),
            prefsGetInt(ConfEntry.prnPrinterPower),
            prefsGetString(ConfEntry.prnPrinterName),
            prefsGetInt(ConfEntry.prnColOffset),
            prefsGetInt(ConfEntry.prnRowOffset)
        )
    }

    fun getBarcodeLabel(q: Int): String {
        var qty = q
        if (qty < 1) {
            qty = 1
        }

        var modStr = templateStr
        for (field in barcodeFields!!) {
            modStr = modStr.replace(
                "${Statics.RESERVED_CHAR}${field.name}${Statics.RESERVED_CHAR}",
                field.value
            )
        }

        modStr = modStr.replace(
            "${Statics.RESERVED_CHAR}${DefinedField.PRINT_SPEED.name}${Statics.RESERVED_CHAR}",
            printOps!!.printerSpeed.toString().padStart(1, '0')
        )
        modStr = modStr.replace(
            "${Statics.RESERVED_CHAR}${DefinedField.PRINT_POWER.name}${Statics.RESERVED_CHAR}",
            printOps!!.printerPower.toString().padStart(2, '0')
        )
        modStr = modStr.replace(
            "${Statics.RESERVED_CHAR}${DefinedField.PRINT_COPIES.name}${Statics.RESERVED_CHAR}",
            qty.toString().padStart(5, '0')
        )
        modStr = modStr.replace(
            "${Statics.RESERVED_CHAR}${DefinedField.COL_OFFSET.name}${Statics.RESERVED_CHAR}",
            printOps!!.colOffset.toString().padStart(4, '0')
        )
        modStr = modStr.replace(
            "${Statics.RESERVED_CHAR}${DefinedField.ROW_OFFSET.name}${Statics.RESERVED_CHAR}",
            printOps!!.rowOffset.toString().padStart(4, '0')
        )

        // Add newline just in case that is missing, so the last command gets executed
        return "$modStr\n"
    }
}