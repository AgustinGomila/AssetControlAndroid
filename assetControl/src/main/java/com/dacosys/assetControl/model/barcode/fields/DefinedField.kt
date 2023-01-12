package com.dacosys.assetControl.model.barcode.fields

import com.dacosys.assetControl.model.barcode.BarcodeLabelTarget
import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

class DefinedField(
    definedFieldId: Long,
    name: String,
    description: String,
    target: BarcodeLabelTarget?,
) {
    var id = 0L
    var name = ""
    var description = ""
    var barcodeLabelTarget: BarcodeLabelTarget? = null

    init {
        this.name = name
        this.description = description
        this.id = definedFieldId
        this.barcodeLabelTarget = target
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DefinedField) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    class CustomComparator : Comparator<DefinedField> {
        override fun compare(
            o1: DefinedField,
            o2: DefinedField,
        ): Int {
            if (o1.id < o2.id) {
                return -1
            } else if (o1.id > o2.id) {
                return 1
            }
            return 0
        }
    }

    companion object {
        var ASSET_ID = DefinedField(
            1,
            "ASSET_ID",
            "Activo fijo Id",
            BarcodeLabelTarget.Asset
        )

        var CODE = DefinedField(
            2,
            "CODE",
            "Código",
            BarcodeLabelTarget.Asset
        )

        var EAN = DefinedField(
            3,
            "EAN",
            "Ean",
            BarcodeLabelTarget.Asset
        )

        var DESCRIPTION = DefinedField(
            4,
            "DESCRIPTION",
            "Descripción",
            BarcodeLabelTarget.Asset
        )

        private var PRICE = DefinedField(
            5,
            "PRICE",
            "Precio",
            BarcodeLabelTarget.Asset
        )

        var ASSET_WAREHOUSE = DefinedField(
            6,
            "WAREHOUSE",
            "Depósito",
            BarcodeLabelTarget.Asset
        )

        var ASSET_WAREHOUSE_AREA = DefinedField(
            7,
            "WAREHOUSE_AREA",
            "Área",
            BarcodeLabelTarget.Asset
        )

        var ORIGINAL_WAREHOUSE = DefinedField(
            8,
            "ORIGINAL_WAREHOUSE",
            "Depósito original",
            BarcodeLabelTarget.Asset
        )

        var ORIGINAL_WAREHOUSE_AREA = DefinedField(
            9,
            "ORIGINAL_WAREHOUSE_AREA",
            "Área original",
            BarcodeLabelTarget.Asset
        )

        var MANUFACTURER = DefinedField(
            10,
            "MANUFACTURER",
            "Fabricante",
            BarcodeLabelTarget.Asset
        )

        var MODEL = DefinedField(
            11,
            "MODEL",
            "Modelo",
            BarcodeLabelTarget.Asset
        )

        var SERIAL_NUMBER = DefinedField(
            12,
            "SERIAL_NUMBER",
            "Número de serie",
            BarcodeLabelTarget.Asset
        )

        private var WARRANTY_DUE = DefinedField(
            13,
            "WARRANTY_DUE",
            "Fecha de vencimiento",
            BarcodeLabelTarget.Asset
        )

        var OBS = DefinedField(
            14,
            "OBS",
            "Observaciones",
            BarcodeLabelTarget.Asset
        )

        var OWNERSHIP = DefinedField(
            15,
            "OWNERSHIP",
            "Propietario",
            BarcodeLabelTarget.Asset
        )

        var STATUS = DefinedField(
            16,
            "STATUS",
            "Estado",
            BarcodeLabelTarget.Asset
        )

        var ITEM_CATEGORY = DefinedField(
            17,
            "ITEM_CATEGORY",
            "Categoría",
            BarcodeLabelTarget.Asset
        )

        var PROVIDER = DefinedField(
            18,
            "PROVIDER",
            "Proveedor",
            BarcodeLabelTarget.Asset
        )

        var ASSET_DATE_PRINTED = DefinedField(
            19,
            "DATE_PRINTED",
            "Fecha de impresión",
            BarcodeLabelTarget.Asset
        )

        // endregion

        // region LOCATION 100-200

        var WAREHOUSE = DefinedField(
            100,
            "WAREHOUSE",
            "Depósito",
            BarcodeLabelTarget.WarehouseArea
        )

        var WAREHOUSE_AREA = DefinedField(
            101,
            "WAREHOUSE_AREA",
            "Área",
            BarcodeLabelTarget.WarehouseArea
        )

        var WAREHOUSE_AREA_ID = DefinedField(
            102,
            "WAREHOUSE_AREA_ID",
            "Área Id",
            BarcodeLabelTarget.WarehouseArea
        )

        var WAREHOUSE_AREA_DATE_PRINTED = DefinedField(
            103,
            "DATE_PRINTED",
            "Fecha de impresión",
            BarcodeLabelTarget.WarehouseArea
        )

        // endregion

        // region GENERAL 200-300

        var PRINT_SPEED = DefinedField(
            200,
            "PRINT_SPEED",
            "Imp: Velocidad",
            null
        )

        var PRINT_POWER = DefinedField(
            201,
            "PRINT_POWER",
            "Imp: Potencia",
            null
        )

        var PRINT_COPIES = DefinedField(
            202,
            "PRINT_COPIES",
            "Imp: Copias",
            null
        )

        var COL_OFFSET = DefinedField(
            203,
            "COL_OFFSET",
            "Imp: Desplazamiento X",
            null
        )

        var ROW_OFFSET = DefinedField(
            204,
            "ROW_OFFSET",
            "Imp: Desplazamiento Y",
            null
        )

        // endregion
        fun getByName(name: String): DefinedField? {
            return getAll().firstOrNull { it.name == name }
        }

        fun getByTarget(target: BarcodeLabelTarget): ArrayList<DefinedField> {
            val result: ArrayList<DefinedField> = ArrayList()
            getAll().filterTo(result) { it.barcodeLabelTarget == target }

            return result
        }

        fun getAll(): ArrayList<DefinedField> {
            val allSections = ArrayList<DefinedField>()
            allSections.addAll(getAsset())
            allSections.addAll(getLocation())
            allSections.addAll(getPrinter())

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAsset(): ArrayList<DefinedField> {
            val allSections = ArrayList<DefinedField>()
            Collections.addAll(
                allSections,
                ASSET_ID,
                CODE,
                EAN,
                DESCRIPTION,
                PRICE,
                ASSET_WAREHOUSE,
                ASSET_WAREHOUSE_AREA,
                ORIGINAL_WAREHOUSE,
                ORIGINAL_WAREHOUSE_AREA,
                MANUFACTURER,
                MODEL,
                SERIAL_NUMBER,
                WARRANTY_DUE,
                OBS,
                OWNERSHIP,
                STATUS,
                ITEM_CATEGORY,
                PROVIDER,
                ASSET_DATE_PRINTED
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getLocation(): ArrayList<DefinedField> {
            val allSections = ArrayList<DefinedField>()
            Collections.addAll(
                allSections,
                WAREHOUSE,
                WAREHOUSE_AREA,
                WAREHOUSE_AREA_ID,
                WAREHOUSE_AREA_DATE_PRINTED
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getPrinter(): ArrayList<DefinedField> {
            val allSections = ArrayList<DefinedField>()
            Collections.addAll(
                allSections,
                PRINT_SPEED,
                PRINT_POWER,
                PRINT_COPIES,
                COL_OFFSET,
                ROW_OFFSET
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(permissionId: Long): DefinedField? {
            return getAll().firstOrNull { it.id == permissionId }
        }
    }
}