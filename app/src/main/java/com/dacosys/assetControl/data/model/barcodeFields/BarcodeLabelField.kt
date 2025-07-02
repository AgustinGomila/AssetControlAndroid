package com.dacosys.assetControl.data.model.barcodeFields

import java.util.*

class BarcodeLabelField {
    var name: String = ""
    var value: String = ""

    constructor(definedField: DefinedField, v: String) {
        var t = v
        name = definedField.name

        t = t.replace("\"", "'").uppercase(Locale.ROOT)
        t = t.replace("Ñ", "N")
        t = t.replace("Ú", "U")
        t = t.replace("Ó", "O")
        t = t.replace("Í", "I")
        t = t.replace("É", "E")
        t = t.replace("Á", "A")

        value = t
    }

    constructor(definedField: DefinedField, v: String, length: Int) {
        var t = v
        name = definedField.name

        t = t.replace("\"", "'").uppercase(Locale.ROOT)
        t = t.replace("Ñ", "N")
        t = t.replace("Ú", "U")
        t = t.replace("Ó", "O")
        t = t.replace("Í", "I")
        t = t.replace("É", "E")
        t = t.replace("Á", "A")

        value = t.take(length)
    }
}