package com.dacosys.assetControl.model.assets.units.unitType

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.assets.units.unitTypeCategory.UnitTypeCategory
import java.util.*

class UnitType : Parcelable {
    var id: Int = 0
    var description: String = ""
    var unitTypeCategory: UnitTypeCategory? = null

    constructor(
        unitTypeId: Int,
        description: String,
        unitTypeCategory: UnitTypeCategory,
    ) {
        this.description = description
        this.id = unitTypeId
        this.unitTypeCategory = unitTypeCategory
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is UnitType) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString() ?: ""
        unitTypeCategory = parcel.readParcelable(UnitTypeCategory::class.java.classLoader)
    }

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

        // temperature units
        var Celsius = UnitType(
            1,
            Statics.AssetControl.getContext().getString(R.string.unit_type_celsius),
            UnitTypeCategory.temperature
        )
        private var Fahrenheit = UnitType(
            2,
            Statics.AssetControl.getContext().getString(R.string.unit_type_fahrenheit),
            UnitTypeCategory.temperature
        )
        private var Kelvin = UnitType(
            3,
            Statics.AssetControl.getContext().getString(R.string.unit_type_kelvin),
            UnitTypeCategory.temperature
        )
        private var Reaumur = UnitType(
            4,
            Statics.AssetControl.getContext().getString(R.string.unit_type_reaumur),
            UnitTypeCategory.temperature
        )
        private var Rankine = UnitType(
            5,
            Statics.AssetControl.getContext().getString(R.string.unit_type_rankine),
            UnitTypeCategory.temperature
        )

        //weight units //weight units
        var Kilogram = UnitType(
            101,
            Statics.AssetControl.getContext().getString(R.string.unit_type_kilogram),
            UnitTypeCategory.weight
        )
        private var Pound = UnitType(
            102,
            Statics.AssetControl.getContext().getString(R.string.unit_type_pound),
            UnitTypeCategory.weight
        )
        private var Grams = UnitType(
            103,
            Statics.AssetControl.getContext().getString(R.string.unit_type_grams),
            UnitTypeCategory.weight
        )
        private var Milligrams = UnitType(
            104,
            Statics.AssetControl.getContext().getString(R.string.unit_type_milligrams),
            UnitTypeCategory.weight
        )

        //lenght units //lenght units
        var Meter = UnitType(
            201,
            Statics.AssetControl.getContext().getString(R.string.unit_type_meter),
            UnitTypeCategory.lenght
        )
        private var Yard = UnitType(
            202,
            Statics.AssetControl.getContext().getString(R.string.unit_type_yard),
            UnitTypeCategory.lenght
        )
        private var Foot = UnitType(
            203,
            Statics.AssetControl.getContext().getString(R.string.unit_type_foot),
            UnitTypeCategory.lenght
        )
        private var Inch = UnitType(
            204,
            Statics.AssetControl.getContext().getString(R.string.unit_type_inch),
            UnitTypeCategory.lenght
        )
        private var Centimeter = UnitType(
            205,
            Statics.AssetControl.getContext().getString(R.string.unit_type_centimeter),
            UnitTypeCategory.lenght
        )
        private var Mile = UnitType(
            206,
            Statics.AssetControl.getContext().getString(R.string.unit_type_mile),
            UnitTypeCategory.lenght
        )

        //volume units //volume units
        var Litre = UnitType(
            301,
            Statics.AssetControl.getContext().getString(R.string.unit_type_litre),
            UnitTypeCategory.volume
        )
        private var Millilitre = UnitType(
            302,
            Statics.AssetControl.getContext().getString(R.string.unit_type_millilitre),
            UnitTypeCategory.volume
        )
        private var Gallon = UnitType(
            303,
            Statics.AssetControl.getContext().getString(R.string.unit_type_gallon),
            UnitTypeCategory.volume
        )
        private var Pint = UnitType(
            304,
            Statics.AssetControl.getContext().getString(R.string.unit_type_pint),
            UnitTypeCategory.volume
        )
        private var CubicInches = UnitType(
            305,
            Statics.AssetControl.getContext().getString(R.string.unit_type_cubicinches),
            UnitTypeCategory.volume
        )

        //quantity units //quantity units
        private var Cake = UnitType(
            401,
            Statics.AssetControl.getContext().getString(R.string.unit_type_cake),
            UnitTypeCategory.quantity
        )
        private var Strip = UnitType(
            402,
            Statics.AssetControl.getContext().getString(R.string.unit_type_strip),
            UnitTypeCategory.quantity
        )
        private var Unit = UnitType(
            403,
            Statics.AssetControl.getContext().getString(R.string.unit_type_unit),
            UnitTypeCategory.quantity
        )

        //area units //area units
        private var Acre = UnitType(
            501,
            Statics.AssetControl.getContext().getString(R.string.unit_type_acre),
            UnitTypeCategory.area
        )
        private var Hectare = UnitType(
            502,
            Statics.AssetControl.getContext().getString(R.string.unit_type_hectare),
            UnitTypeCategory.area
        )
        var SquareMeter = UnitType(
            503,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_meter),
            UnitTypeCategory.area
        )
        private var SquareKilometer = UnitType(
            504,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_kilometer),
            UnitTypeCategory.area
        )
        private var SquareCentimeter = UnitType(
            505,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_centimeter),
            UnitTypeCategory.area
        )
        private var SquareFoot = UnitType(
            506,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_foot),
            UnitTypeCategory.area
        )
        private var SquareYard = UnitType(
            507,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_yard),
            UnitTypeCategory.area
        )
        private var SquareInch = UnitType(
            508,
            Statics.AssetControl.getContext().getString(R.string.unit_type_square_inch),
            UnitTypeCategory.area
        )

        //pressure units //pressure units
        private var KilopondSquareCentimeter = UnitType(
            601,
            Statics.AssetControl.getContext()
                .getString(R.string.unit_type_kilopond_square_centimeter),
            UnitTypeCategory.pressure
        ) //"Kilogramo fuerza por centímetro cuadradro"
        var Pascal = UnitType(
            602,
            Statics.AssetControl.getContext().getString(R.string.unit_type_pascal),
            UnitTypeCategory.pressure
        ) // Pascal
        private var Bar = UnitType(
            603,
            Statics.AssetControl.getContext().getString(R.string.unit_type_bar),
            UnitTypeCategory.pressure
        )
        private var TechnicalAtmosphere = UnitType(
            604,
            Statics.AssetControl.getContext().getString(R.string.unit_type_technical_atmosphere),
            UnitTypeCategory.pressure
        ) //Atmósfera técnica
        private var StandardAtmosphere = UnitType(
            605,
            Statics.AssetControl.getContext().getString(R.string.unit_type_standard_atmosphere),
            UnitTypeCategory.pressure
        ) //Atmósfera estándar
        private var Torr = UnitType(
            606,
            Statics.AssetControl.getContext().getString(R.string.unit_type_torr),
            UnitTypeCategory.pressure
        ) //Torr
        private var NewtonSquareMilimeter = UnitType(
            607,
            Statics.AssetControl.getContext().getString(R.string.unit_type_newton_square_milimeter),
            UnitTypeCategory.pressure
        ) //"Newton por milímetro cuadrado"
        private var KilopondSquareMeter = UnitType(
            608,
            Statics.AssetControl.getContext().getString(R.string.unit_type_kilopond_square_meter),
            UnitTypeCategory.pressure
        ) //Kilogramo fuerza por metro cuadradro"

        fun getAll(): ArrayList<UnitType> {
            val allSections = ArrayList<UnitType>()
            Collections.addAll(
                allSections,
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
                NewtonSquareMilimeter,
                KilopondSquareMeter
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getByUnitTypeCategory(unitTypeCategory: UnitTypeCategory): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == unitTypeCategory }
        }

        fun getAllTemperature(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.temperature }
        }

        fun getAllPressure(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.pressure }
        }

        fun getAllArea(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.area }
        }

        fun getAllQuantity(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.quantity }
        }

        fun getAllVolume(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.volume }
        }

        fun getAllLenght(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.lenght }
        }

        fun getAllWeight(): ArrayList<UnitType> {
            return getAll().filterTo(ArrayList()) { it.unitTypeCategory == UnitTypeCategory.weight }
        }

        fun getById(unitTypeId: Int): UnitType? {
            return getAll().firstOrNull { it.id == unitTypeId }
        }
    }
}