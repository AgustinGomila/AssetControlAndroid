package com.example.assetControl.data.enums.attribute

import android.os.Parcel
import android.os.Parcelable
import com.example.assetControl.data.enums.unit.UnitType
import com.example.assetControl.data.room.dto.asset.Asset
import java.sql.Time
import java.util.*

data class AttributeCompositionType(
    var id: Long = 0L,
    var description: String = "",
    var type: Any? = null,
    var defaultValue: Any? = null,
    var resourceString: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().orEmpty()
    )

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
            resourceString = "unit_type_length"
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

        var TypeUnitPressure = AttributeCompositionType(
            18,
            description = "Unidad de presión",
            type = UnitType::class.javaObjectType,
            defaultValue = UnitType.Pascal,
            resourceString = "unit_type_pressure"
        )

        fun getAll(): List<AttributeCompositionType> {
            return listOf(
                TypeTextShort,
                TypeBool,
                TypeIntNumber,
                TypeDecimalNumber,
                TypeOptions,
                TypeCurrency,
                TypeTextLong,
                TypeDate,
                TypeTime,
                TypeUnitWeight,
                TypeUnitVolume,
                TypeUnitPressure,
                TypeUnitArea,
                TypeUnitLenght,
                TypeUnitTemperature,
                TypeAsset,
            )
        }

        fun getById(id: Long): AttributeCompositionType? {
            return getAll().firstOrNull { it.id == id }
        }

        fun getAllUnitType(): List<AttributeCompositionType> {
            return listOf(
                TypeUnitArea,
                TypeUnitVolume,
                TypeUnitLenght,
                TypeUnitPressure,
                TypeUnitTemperature,
                TypeUnitWeight
            )
        }
    }
}




