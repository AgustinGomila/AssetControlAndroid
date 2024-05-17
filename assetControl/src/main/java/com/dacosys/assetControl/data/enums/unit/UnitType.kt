package com.dacosys.assetControl.data.enums.unit

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class UnitType(val id: Int, val description: String, val unitTypeCategory: UnitTypeCategory) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readParcelable(UnitTypeCategory::class.java.classLoader) ?: UnitTypeCategory.unknown
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
        parcel.writeParcelable(unitTypeCategory, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UnitType> {
        override fun createFromParcel(parcel: Parcel): UnitType {
            return UnitType(parcel)
        }

        override fun newArray(size: Int): Array<UnitType?> {
            return arrayOfNulls(size)
        }

        //temperature units
        var Celsius = UnitType(
            1,
            getContext().getString(R.string.unit_type_celsius),
            UnitTypeCategory.temperature
        )
        private var Fahrenheit = UnitType(
            2,
            getContext().getString(R.string.unit_type_fahrenheit),
            UnitTypeCategory.temperature
        )
        private var Kelvin = UnitType(
            3,
            getContext().getString(R.string.unit_type_kelvin),
            UnitTypeCategory.temperature
        )
        private var Reaumur = UnitType(
            4,
            getContext().getString(R.string.unit_type_reaumur),
            UnitTypeCategory.temperature
        )
        private var Rankine = UnitType(
            5,
            getContext().getString(R.string.unit_type_rankine),
            UnitTypeCategory.temperature
        )

        //weight units
        var Kilogram = UnitType(
            101,
            getContext().getString(R.string.unit_type_kilogram),
            UnitTypeCategory.weight
        )
        private var Pound = UnitType(
            102,
            getContext().getString(R.string.unit_type_pound),
            UnitTypeCategory.weight
        )
        private var Grams = UnitType(
            103,
            getContext().getString(R.string.unit_type_grams),
            UnitTypeCategory.weight
        )
        private var Milligrams = UnitType(
            104,
            getContext().getString(R.string.unit_type_milligrams),
            UnitTypeCategory.weight
        )

        //length units
        var Meter = UnitType(
            201,
            getContext().getString(R.string.unit_type_meter),
            UnitTypeCategory.length
        )
        private var Yard = UnitType(
            202,
            getContext().getString(R.string.unit_type_yard),
            UnitTypeCategory.length
        )
        private var Foot = UnitType(
            203,
            getContext().getString(R.string.unit_type_foot),
            UnitTypeCategory.length
        )
        private var Inch = UnitType(
            204,
            getContext().getString(R.string.unit_type_inch),
            UnitTypeCategory.length
        )
        private var Centimeter = UnitType(
            205,
            getContext().getString(R.string.unit_type_centimeter),
            UnitTypeCategory.length
        )
        private var Mile = UnitType(
            206,
            getContext().getString(R.string.unit_type_mile),
            UnitTypeCategory.length
        )

        //volume units
        var Litre = UnitType(
            301,
            getContext().getString(R.string.unit_type_litre),
            UnitTypeCategory.volume
        )
        private var Millilitre = UnitType(
            302,
            getContext().getString(R.string.unit_type_millilitre),
            UnitTypeCategory.volume
        )
        private var Gallon = UnitType(
            303,
            getContext().getString(R.string.unit_type_gallon),
            UnitTypeCategory.volume
        )
        private var Pint = UnitType(
            304,
            getContext().getString(R.string.unit_type_pint),
            UnitTypeCategory.volume
        )
        private var CubicInches = UnitType(
            305,
            getContext().getString(R.string.unit_type_cubicinches),
            UnitTypeCategory.volume
        )

        //quantity units
        private var Cake = UnitType(
            401,
            getContext().getString(R.string.unit_type_cake),
            UnitTypeCategory.quantity
        )
        private var Strip = UnitType(
            402,
            getContext().getString(R.string.unit_type_strip),
            UnitTypeCategory.quantity
        )
        private var Unit = UnitType(
            403,
            getContext().getString(R.string.unit_type_unit),
            UnitTypeCategory.quantity
        )

        //area units
        private var Acre = UnitType(
            501,
            getContext().getString(R.string.unit_type_acre),
            UnitTypeCategory.area
        )
        private var Hectare = UnitType(
            502,
            getContext().getString(R.string.unit_type_hectare),
            UnitTypeCategory.area
        )
        var SquareMeter = UnitType(
            503,
            getContext().getString(R.string.unit_type_square_meter),
            UnitTypeCategory.area
        )
        private var SquareKilometer = UnitType(
            504,
            getContext().getString(R.string.unit_type_square_kilometer),
            UnitTypeCategory.area
        )
        private var SquareCentimeter = UnitType(
            505,
            getContext().getString(R.string.unit_type_square_centimeter),
            UnitTypeCategory.area
        )
        private var SquareFoot = UnitType(
            506,
            getContext().getString(R.string.unit_type_square_foot),
            UnitTypeCategory.area
        )
        private var SquareYard = UnitType(
            507,
            getContext().getString(R.string.unit_type_square_yard),
            UnitTypeCategory.area
        )
        private var SquareInch = UnitType(
            508,
            getContext().getString(R.string.unit_type_square_inch),
            UnitTypeCategory.area
        )

        //pressure units
        private var KilopondSquareCentimeter = UnitType(
            601,
            getContext()
                .getString(R.string.unit_type_kilopond_square_centimeter),
            UnitTypeCategory.pressure
        ) //"Kilogramo fuerza por centímetro cuadradro"
        var Pascal = UnitType(
            602,
            getContext().getString(R.string.unit_type_pascal),
            UnitTypeCategory.pressure
        ) // Pascal
        private var Bar = UnitType(
            603,
            getContext().getString(R.string.unit_type_bar),
            UnitTypeCategory.pressure
        )
        private var TechnicalAtmosphere = UnitType(
            604,
            getContext().getString(R.string.unit_type_technical_atmosphere),
            UnitTypeCategory.pressure
        ) //Atmósfera técnica
        private var StandardAtmosphere = UnitType(
            605,
            getContext().getString(R.string.unit_type_standard_atmosphere),
            UnitTypeCategory.pressure
        ) //Atmósfera estándar
        private var Torr = UnitType(
            606,
            getContext().getString(R.string.unit_type_torr),
            UnitTypeCategory.pressure
        ) //Torr
        private var NewtonSquareMillimeter = UnitType(
            607,
            getContext().getString(R.string.unit_type_newton_square_milimeter),
            UnitTypeCategory.pressure
        ) //"Newton por milímetro cuadrado"
        private var KilopondSquareMeter = UnitType(
            608,
            getContext().getString(R.string.unit_type_kilopond_square_meter),
            UnitTypeCategory.pressure
        ) //"Kilogramo fuerza por metro cuadradro"

        fun getAll(): List<UnitType> {
            return listOf(
                Celsius,
                Fahrenheit,
                Kelvin,
                Reaumur,
                Rankine,
                Kilogram,
                Pound,
                Grams,
                Milligrams,
                Meter,
                Yard,
                Foot,
                Inch,
                Centimeter,
                Mile,
                Litre,
                Millilitre,
                Gallon,
                Pint,
                CubicInches,
                Cake,
                Strip,
                Unit,
                Acre,
                Hectare,
                SquareMeter,
                SquareKilometer,
                SquareCentimeter,
                SquareFoot,
                SquareYard,
                SquareInch,
                KilopondSquareCentimeter,
                Pascal,
                Bar,
                TechnicalAtmosphere,
                StandardAtmosphere,
                Torr,
                NewtonSquareMillimeter,
                KilopondSquareMeter
            )
        }

        fun getById(id: Int): UnitType? {
            return getAll().firstOrNull { it.id == id }
        }

        fun getByCategory(category: UnitTypeCategory): List<UnitType> {
            return getAll().mapNotNull { if (it.unitTypeCategory == category) it else null }.toList()
        }
    }
}

