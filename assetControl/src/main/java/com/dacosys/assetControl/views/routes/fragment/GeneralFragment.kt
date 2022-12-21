package com.dacosys.assetControl.views.routes.fragment

import android.util.Log
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.`object`.AttributeComposition
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeCompositionType.AttributeCompositionType
import com.dacosys.assetControl.model.assets.units.unitType.UnitType
import com.dacosys.assetControl.model.assets.units.unitTypeCategory.UnitTypeCategory
import com.dacosys.assetControl.model.routes.commons.ExprResultIntString
import com.dacosys.assetControl.model.routes.commons.Parameter
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.`object`.DataCollectionRuleContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.udojava.evalex.Expression
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class GeneralFragment(private var dccFragmentListener: DccFragmentListener) {
    override fun equals(other: Any?): Boolean {
        return if (other !is GeneralFragment) {
            false
        } else
            this.level == other.level &&
                    this.position == other.position &&
                    this.attrCompId == other.attrCompId
    }

    fun getFragment(): Any? {
        if (currentFragment == null) {
            val tempDcrCont = dataCollectionRuleContent ?: return null

            if (tempDcrCont.attributeCompositionId > 0) {
                val attrComp = AttributeComposition(
                    id = tempDcrCont.attributeCompositionId,
                    doChecks = false
                )
                generateFragment(attrComp)
            }
        }
        return currentFragment
    }

    private var currentFragment: Any? = null

    private var externalParameters: ArrayList<Parameter> = ArrayList()
    var ownParameters: ArrayList<Parameter> = ArrayList()
    private var separator = '.'

    // Se usa para hacer un reemplazo de los resultados de la expresión que corresponden
    // a una secuencia de niveles por un valor numérico y viceversa.
    private var tempResult: ArrayList<ExprResultIntString> = ArrayList()

    var attributeCompositionType: AttributeCompositionType? = null

    val name: String
        get() = if (dataCollectionRuleContent != null) {
            level.toString() + separator +
                    position.toString() + separator +
                    attrCompId.toString()
        } else {
            ""
        }

    val level: Int
        get() = dataCollectionRuleContent!!.level

    val position: Int
        get() = dataCollectionRuleContent!!.position

    val isAttribute: Boolean
        get() = dataCollectionRuleContent!!.attributeCompositionId <= 0

    val attrCompId: Long
        get() = dataCollectionRuleContent!!.attributeCompositionId

    var dataCollectionRuleContent: DataCollectionRuleContent? = null
        set(value) {
            field = value
            if (value == null) {
                return
            }

            if (value.attributeCompositionId > 0) {
                val attrComp = AttributeComposition(
                    id = value.attributeCompositionId,
                    doChecks = false
                )

                attributeCompositionType = attrComp.attributeCompositionType
            }
        }

    private data class GeneralFragmentValueData(
        val attrCompTypeId: Long?,
        val valueStr: String?,
    )

    fun destroy() {
        saveLastValue()
        Log.d(
            this::class.java.simpleName,
            "Destroying fragment: ${attributeCompositionType.toString()}..."
        )
        currentFragment = null
    }

    private var lastValue: GeneralFragmentValueData? = null
    fun saveLastValue() {
        lastValue = null
        val attrCompType = attributeCompositionType ?: return
        lastValue = GeneralFragmentValueData(attrCompType.id, valueStr)
    }

    private fun generateFragment(attrComp: AttributeComposition) {
        val attrCompType = attributeCompositionType ?: return
        Log.d(this::class.java.simpleName, "Generating fragment: $attrCompType...")

        val lastV = convertStringToTypedAttr(attrCompType, lastValue?.valueStr)

        when {
            attrCompType == AttributeCompositionType.TypeIntNumber -> {
                currentFragment = DecimalFragment.newInstance(
                    decimalPlaces = 0,
                    description = attrComp.description,
                    value = lastV as Float?
                )
                (currentFragment as DecimalFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeDecimalNumber ||
                    attrCompType == AttributeCompositionType.TypeCurrency -> {
                currentFragment = DecimalFragment.newInstance(
                    decimalPlaces = 3,
                    description = attrComp.description,
                    value = lastV as Float?
                )
                (currentFragment as DecimalFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeBool -> {
                currentFragment = BooleanFragment.newInstance(
                    description = attrComp.description,
                    value = lastV as Boolean?
                )
                (currentFragment as BooleanFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeTextLong ||
                    attrCompType == AttributeCompositionType.TypeTextShort -> {
                currentFragment = StringFragment.newInstance(
                    description = attrComp.description,
                    value = lastV as String?
                )
                (currentFragment as StringFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeTime -> {
                currentFragment = TimeFragment.newInstance(
                    description = attrComp.description,
                    value = lastV as Calendar?
                )
                (currentFragment as TimeFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeDate -> {
                currentFragment = DateFragment.newInstance(
                    description = attrComp.description,
                    value = lastV as Calendar?
                )
                (currentFragment as DateFragment).setListener(dccFragmentListener)
            }
            attrCompType == AttributeCompositionType.TypeOptions -> {
                var composition = ""
                if (attrComp.composition != null) {
                    composition = (attrComp.composition ?: return)
                }

                currentFragment = CommaSeparatedSpinnerFragment.newInstance(
                    commaSeparatedOptions = composition,
                    description = attrComp.description,
                    value = lastV as String?
                )
                (currentFragment as CommaSeparatedSpinnerFragment).setListener(dccFragmentListener)
            }
            AttributeCompositionType.getAllUnitType().contains(attrCompType)
            -> {
                when (attrCompType) {
                    AttributeCompositionType.TypeUnitVolume -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.volume,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                    AttributeCompositionType.TypeUnitWeight -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.weight,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                    AttributeCompositionType.TypeUnitTemperature -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.temperature,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                    AttributeCompositionType.TypeUnitPressure -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.pressure,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                    AttributeCompositionType.TypeUnitLenght -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.lenght,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                    AttributeCompositionType.TypeUnitArea -> {
                        currentFragment = UnitTypeSpinnerFragment.newInstance(
                            unitTypeCat = UnitTypeCategory.area,
                            description = attrComp.description,
                            value = lastV as UnitType?
                        )
                        (currentFragment as UnitTypeSpinnerFragment).setListener(dccFragmentListener)
                    }
                }
            }
        }
    }

    fun evaluate(): Any? {
        try {
            val tempDcrCont = dataCollectionRuleContent ?: return null

            if (tempDcrCont.attributeCompositionId > 0) {
                addOwnParameter(tempDcrCont, valueStr)
            }

            val tempExp = tempDcrCont.expression ?: ""
            if (tempExp.trim().isEmpty()) {
                return null
            }

            // Por las dudas, busco si ese parámetro ya existe en la colección de parámetros externos
            // y lo reemplazo por el nuevo
            val keysToRemove: ArrayList<String> = ArrayList()
            for (extParam in externalParameters) {
                for (ownParam in ownParameters) {
                    if (ownParam.paramName == extParam.paramName) {
                        keysToRemove.add(extParam.paramName)
                        break
                    }
                }
            }

            for (keys in keysToRemove) {
                externalParameters.remove(externalParameters.first { it.paramName == keys })
            }

            val e: Expression
            try {
                val allParam: ArrayList<Parameter> = ArrayList()
                for (own in ownParameters) {
                    allParam.add(own)
                }

                for (ext in externalParameters) {
                    allParam.add(ext)
                }

                e = formatExpression(tempExp, allParam)
            } catch (ex: Exception) {
                return null
            }

            return try {
                val res = e.eval().toInt()
                tempResult
                    .firstOrNull { it.key == res }
                    ?.value
                    ?: res
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                null
            }
        } catch (ex: Exception) {
            return null
        } finally {
            /*
            if (EvaluateNeeded != null) {
                EvaluateNeeded(this, new EventArgs ());}
            */
        }
    }

    private fun formatExpression(exp: String, par: ArrayList<Parameter>): Expression {
        val value = "A"
        var charValue = value[0].code
        var expression = exp
        val parameters: ArrayList<Parameter> = ArrayList()

        val attrCompDbHelper = AttributeCompositionDbHelper()

        for (p in par) {
            val nextLetter = charValue.toChar().toString()
            charValue++

            // Reemplazar los valores de texto por valores númericos que puedan ser comparados
            // a partir del AttributeCompositionId del ParamName
            val attrCompId = p.paramName.split('.').last().toLong()
            val attrComp = attrCompDbHelper.selectById(attrCompId)
            var pValue: Int

            if (attrComp!!.attributeCompositionTypeId == AttributeCompositionType.TypeOptions.id) {
                var composition = ""
                if (attrComp.composition != null) {
                    composition = attrComp.composition!!.trim().trimEnd(';')
                }

                val allOptions = ArrayList(composition.split(';')).sorted()

                // Reemplazo los valores en texto por un Id a fin de poder ser comparados
                for (a in allOptions) {
                    pValue = allOptions.indexOf(a)
                    expression = expression.replace("'$a'", pValue.toString())
                }

                pValue = allOptions.indexOf(p.paramValue)
                parameters.add(Parameter(nextLetter, pValue))
            } else {
                parameters.add(Parameter(nextLetter, p.paramValue))
            }
            expression = expression.replace("[" + p.paramName + "]", nextLetter)
        }

        // Reemplazar los resultados de tipo secuencia de niveles (ej: '3,4')
        // por un valor númerico falso para poder devolverse como resultado
        var pat = Pattern.compile("'([^']*)'")
        var fakeValue = 9000
        var m = pat.matcher(expression)
        tempResult.clear()

        while (m.find()) {
            fakeValue++
            tempResult.add(ExprResultIntString(fakeValue, m.group(1) ?: ""))

            expression = expression.replace("'" + m.group(1) + "'", fakeValue.toString())
        }

        // Finalmente, reemplazar todos aquellos parámetros que no fueron reemplazados antes
        // porque esos datos aún no fueron registrados
        pat = Pattern.compile("""\[([^]]+)]""")
        m = pat.matcher(expression)
        while (m.find()) {
            expression = expression.replace("[" + m.group(1) + "]", "ZZ")
        }

        val e = Expression(expression)
        for (p in parameters) {
            e.setVariable(p.paramName, p.paramValue.toString())
        }

        e.setVariable("ZZ", 999999.toString())

        return e
    }

    private fun addOwnParameter(dcrc: DataCollectionRuleContent, value: Any?) {
        val paramName = dcrc.level.toString() + separator +
                dcrc.position + separator +
                dcrc.attributeCompositionId

        ownParameters.remove(ownParameters.first { it.paramName == paramName })
        ownParameters.add(Parameter(paramName, value))
    }

    fun clearExternalParameters() {
        externalParameters.clear()
    }

    @Suppress("unused")
    fun addExternalParameter(parameters: ArrayList<Parameter>) {
        for (param in parameters) {
            addExternalParameter(param)
        }
    }

    fun addExternalParameter(param: Parameter) {
        externalParameters.remove(externalParameters.first { it.paramName == param.paramName })
        externalParameters.add(param)
    }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            setFragmentEnables(value)
        }

    private fun setFragmentEnables(isEnabled: Boolean) {
        val attrCompType = attributeCompositionType ?: return

        val f = getFragment()
        when {
            attrCompType == AttributeCompositionType.TypeIntNumber ||
                    attrCompType == AttributeCompositionType.TypeDecimalNumber ||
                    attrCompType == AttributeCompositionType.TypeCurrency -> {
                (f as DecimalFragment).isEnabled = isEnabled
            }
            attrCompType == AttributeCompositionType.TypeBool -> {
                (f as BooleanFragment).isEnabled = isEnabled
            }
            attrCompType == AttributeCompositionType.TypeTextLong ||
                    attrCompType == AttributeCompositionType.TypeTextShort -> {
                (f as StringFragment).isEnabled = isEnabled
            }
            attrCompType == AttributeCompositionType.TypeTime -> {
                (f as TimeFragment).isEnabled = isEnabled
            }
            attrCompType == AttributeCompositionType.TypeDate -> {
                (f as DateFragment).isEnabled = isEnabled
            }
            attrCompType == AttributeCompositionType.TypeOptions -> {
                (f as CommaSeparatedSpinnerFragment).isEnabled = isEnabled
            }
            AttributeCompositionType.getAllUnitType().contains(attrCompType) -> {
                (f as UnitTypeSpinnerFragment).isEnabled = isEnabled
            }
        }
    }

    override fun hashCode(): Int {
        var result = level
        result = 31 * result + position
        return result
    }

    fun getFragmentData(): FragmentData {
        return FragmentData(
            dcrContId = dataCollectionRuleContent?.dataCollectionRuleContentId,
            attrCompTypeId = attributeCompositionType?.id,
            valueStr = lastValue?.valueStr.toString(),
            isEnabled = isEnabled
        )
    }

    data class FragmentData(
        val dcrContId: Long?,
        val attrCompTypeId: Long?,
        val valueStr: String,
        val isEnabled: Boolean,
    )

    var valueStr: String? = null
        set(value) {
            if (isAttribute) return
            field = value
            val attrCompType = attributeCompositionType ?: return
            lastValue = GeneralFragmentValueData(attrCompType.id, value)

            val typedV = convertStringToTypedAttr(attrCompType, value)

            val f = getFragment()
            when {
                value == null -> {
                }
                attrCompType == AttributeCompositionType.TypeIntNumber ||
                        attrCompType == AttributeCompositionType.TypeDecimalNumber ||
                        attrCompType == AttributeCompositionType.TypeCurrency -> {
                    (f as DecimalFragment).defaultValue = typedV as Float
                    f.value = typedV
                }
                attrCompType == AttributeCompositionType.TypeBool -> {
                    (f as BooleanFragment).defaultValue = typedV as Boolean
                    f.value = typedV
                }
                attrCompType == AttributeCompositionType.TypeTextLong ||
                        attrCompType == AttributeCompositionType.TypeTextShort -> {
                    (f as StringFragment).defaultValue = typedV as String
                    f.value = typedV
                }
                attrCompType == AttributeCompositionType.TypeTime -> {
                    (f as TimeFragment).defaultValue = value
                    f.value = typedV as Calendar
                }
                attrCompType == AttributeCompositionType.TypeDate -> {
                    (f as DateFragment).defaultValue = value
                    f.value = typedV as Calendar
                }
                attrCompType == AttributeCompositionType.TypeOptions -> {
                    (f as CommaSeparatedSpinnerFragment).defaultValue = typedV as String
                    f.selectedStrOption = typedV
                }
                AttributeCompositionType.getAllUnitType().contains(attrCompType) -> {
                    (f as UnitTypeSpinnerFragment).defaultValue = typedV as UnitType
                    f.selectedUnitType = typedV
                }
            }
        }
        get() {
            if (isAttribute) return null
            val attrCompType = attributeCompositionType ?: return null
            val f = getFragment()

            return when {
                attrCompType == AttributeCompositionType.TypeIntNumber ||
                        attrCompType == AttributeCompositionType.TypeDecimalNumber ||
                        attrCompType == AttributeCompositionType.TypeCurrency -> {
                    (f as DecimalFragment).value.toString()
                }
                attrCompType == AttributeCompositionType.TypeBool -> {
                    (f as BooleanFragment).value.toString()
                }
                attrCompType == AttributeCompositionType.TypeTextLong ||
                        attrCompType == AttributeCompositionType.TypeTextShort -> {
                    (f as StringFragment).value
                }
                attrCompType == AttributeCompositionType.TypeTime -> {
                    val cal = (f as TimeFragment).value
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    dateFormat.format(cal.time)
                }
                attrCompType == AttributeCompositionType.TypeDate -> {
                    val cal = (f as DateFragment).value
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                    dateFormat.format(cal.time)
                }
                attrCompType == AttributeCompositionType.TypeOptions -> {
                    (f as CommaSeparatedSpinnerFragment).selectedStrOption
                }
                AttributeCompositionType.getAllUnitType().contains(attrCompType) -> {
                    (f as UnitTypeSpinnerFragment).selectedUnitType.toString()
                }
                else -> {
                    null
                }
            }
        }

    companion object {
        private fun convertStringToTypedAttr(
            attributeCompositionType: AttributeCompositionType,
            value: String?,
        ): Any? {
            var r: Any? = null
            when {
                value == null -> {
                }
                attributeCompositionType == AttributeCompositionType.TypeIntNumber ||
                        attributeCompositionType == AttributeCompositionType.TypeDecimalNumber ||
                        attributeCompositionType == AttributeCompositionType.TypeCurrency -> {
                    r = value.toFloatOrNull() ?: 0f
                }
                attributeCompositionType == AttributeCompositionType.TypeBool -> {
                    r = value.toBoolean()
                }
                attributeCompositionType == AttributeCompositionType.TypeTextLong ||
                        attributeCompositionType == AttributeCompositionType.TypeTextShort -> {
                    r = value
                }
                attributeCompositionType == AttributeCompositionType.TypeTime -> {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    try {
                        cal.time = sdf.parse(value) ?: return cal
                    } catch (ex: java.lang.Exception) {
                        val date = sdf.parse("12:00") ?: return cal
                        cal.time = date
                    }
                    r = cal
                }
                attributeCompositionType == AttributeCompositionType.TypeDate -> {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                    try {
                        cal.time = sdf.parse(value) ?: return cal
                    } catch (ex: java.lang.Exception) {
                        Log.e(Companion::class.java.simpleName, ex.message.toString())
                    }
                    r = cal
                }
                attributeCompositionType == AttributeCompositionType.TypeOptions -> {
                    r = value
                }
                AttributeCompositionType.getAllUnitType()
                    .contains(attributeCompositionType) -> {
                    val uT = UnitType.getById(value.toInt())
                    r = uT
                }
            }
            return r
        }
    }
}