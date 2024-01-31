package com.dacosys.assetControl.data.model.attribute

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.data.model.asset.Asset
import com.dacosys.assetControl.data.model.asset.UnitType
import java.lang.reflect.Type
import java.sql.Time
import java.util.*

class AttributeCompositionType : Parcelable {
    var id: Long = 0
    private var description: String = ""
    private var type: Any? = null
    private var defaultValue: Any? = null
    private var resourceString: String = ""

    constructor(
        attributeCompositionTypeId: Long,
        description: String,
        type: Type,
        defaultValue: Any?,
        resourceString: String,
    ) {
        this.description = description
        this.id = attributeCompositionTypeId
        this.type = type
        this.defaultValue = defaultValue
        this.resourceString = resourceString
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is AttributeCompositionType) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttributeCompositionType> {
        override fun createFromParcel(parcel: Parcel): AttributeCompositionType {
            return AttributeCompositionType(parcel)
        }

        override fun newArray(size: Int): Array<AttributeCompositionType?> {
            return arrayOfNulls(size)
        }

        var TypeTextShort = AttributeCompositionType(
            1,
            description = "Texto (hasta 255 caracteres)",
            type = String::class.javaObjectType,
            defaultValue = "",
            resourceString = "unit_type_text_short"
        )

        var TypeBool = AttributeCompositionType(
            2,
            description = "Verdadero / Falso",
            type = Boolean::class.javaObjectType,
            defaultValue = true,
            resourceString = "unit_type_bool"
        )

        var TypeIntNumber = AttributeCompositionType(
            3,
            description = "Número entero",
            type = Int::class.javaObjectType,
            defaultValue = 0,
            resourceString = "unit_type_int_number"
        )

        var TypeDecimalNumber = AttributeCompositionType(
            4,
            description = "Número decimal",
            type = Float::class.javaObjectType,
            defaultValue = 0.0,
            resourceString = "unit_type_decimal_number"
        )

        var TypeOptions = AttributeCompositionType(
            5,
            description = "Opciones (separadas por ;)",
            type = String::class.javaObjectType,
            defaultValue = "",
            resourceString = "unit_type_options"
        )

        /*
        var TypeSql = AttributeCompositionType(6,
                "SQL (1º descripción, 2º valor)",
                String::class.javaObjectType,
                "" ,
                "unit_type_sql")
                */

        var TypeCurrency = AttributeCompositionType(
            7,
            description = "Moneda",
            type = Float::class.javaObjectType,
            defaultValue = 0.0,
            resourceString = "unit_type_currency"
        )

        var TypeTextLong = AttributeCompositionType(
            8,
            description = "Texto largo",
            type = String::class.javaObjectType,
            defaultValue = "",
            resourceString = "unit_type_text_long"
        )

        var TypeDate = AttributeCompositionType(
            9,
            description = "Fecha",
            type = Date::class.javaObjectType,
            defaultValue = "2001/01/01",
            resourceString = "unit_type_date"
        )

        var TypeTime = AttributeCompositionType(
            10,
            description = "Hora",
            type = Time::class.javaObjectType,
            defaultValue = "00:00:00",
            resourceString = "unit_type_time"
        )

        var TypeUnitWeight = AttributeCompositionType(
            11,
            description = "Unidad de peso",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Kilogram,
            resourceString = "unit_type_weight"
        )

        var TypeUnitVolume = AttributeCompositionType(
            12,
            description = "Unidad de volumen",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Litre,
            resourceString = "unit_type_volume"
        )

        var TypeUnitArea = AttributeCompositionType(
            13,
            description = "Unidad de superficie",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.SquareMeter,
            resourceString = "unit_type_area"
        )

        var TypeUnitLenght = AttributeCompositionType(
            14,
            description = "Unidad de longitud",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Meter,
            resourceString = "unit_type_lenght"
        )

        var TypeUnitTemperature = AttributeCompositionType(
            15,
            description = "Unidad de temperatura",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Celsius,
            resourceString = "unit_type_temperature"
        )

        var TypeAsset = AttributeCompositionType(
            16,
            description = "Activo",
            type = Asset::class.javaObjectType,
            defaultValue = null,
            resourceString = "unit_type_asset"
        )

        /*
        var TypeEntity = AttributeCompositionType(17,
                "Entidad",
                Provider::class.javaObjectType,
                null ,
                "unit_type_entity")
                */

        var TypeUnitPressure = AttributeCompositionType(
            18,
            description = "Unidad de presión",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Pascal,
            resourceString = "unit_type_pressure"
        )

        fun getAll(): ArrayList<AttributeCompositionType> {
            val allSections = ArrayList<AttributeCompositionType>()
            allSections.add(TypeTextShort)
            allSections.add(TypeBool)
            allSections.add(TypeIntNumber)
            allSections.add(TypeDecimalNumber)
            allSections.add(TypeOptions)
            // allSections.add(TypeSql)
            allSections.add(TypeCurrency)
            allSections.add(TypeTextLong)
            allSections.add(TypeDate)
            allSections.add(TypeTime)
            allSections.add(TypeUnitWeight)
            allSections.add(TypeUnitVolume)
            allSections.add(TypeUnitArea)
            allSections.add(TypeUnitLenght)
            allSections.add(TypeUnitTemperature)
            allSections.add(TypeAsset)
            // allSections.add(TypeEntity)
            allSections.add(TypeUnitPressure)

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAllUnitType(): ArrayList<AttributeCompositionType> {
            val allSections = ArrayList<AttributeCompositionType>()
            allSections.add(TypeUnitArea)
            allSections.add(TypeUnitVolume)
            allSections.add(TypeUnitLenght)
            allSections.add(TypeUnitPressure)
            allSections.add(TypeUnitTemperature)
            allSections.add(TypeUnitWeight)

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(attributeCompositionTypeId: Long): AttributeCompositionType? {
            return getAll().firstOrNull { it.id == attributeCompositionTypeId }
        }
    }
}