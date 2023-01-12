package com.dacosys.assetControl.model.user.permission

import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

class PermissionEntry(
    permissionId: Long,
    description: String,
    permissionCategory: PermissionCategory,
) {
    var id: Long = 0
    var description: String = ""
    private var permissionCategory: PermissionCategory? = null

    init {
        this.description = description
        this.id = permissionId
        this.permissionCategory = permissionCategory
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is PermissionEntry) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    class CustomComparator : Comparator<PermissionEntry> {
        override fun compare(o1: PermissionEntry, o2: PermissionEntry): Int {
            if (o1.id < o2.id) {
                return -1
            } else if (o1.id > o2.id) {
                return 1
            }
            return 0
        }
    }

    companion object {
        // REMEMBER TO ADD THE SAME THING TO THE DATABASE WHEN ADDING A NEW ONE HERE!!
        var AddAsset = PermissionEntry(
            1,
            "Activos, agregar",
            PermissionCategory.Asset
        )
        var ModifyAsset = PermissionEntry(
            2,
            "Activos, modificar",
            PermissionCategory.Asset
        )
        var PrintLabel = PermissionEntry(
            3,
            "Imprimir etiquetas",
            PermissionCategory.Print
        )
        private var PrintReport = PermissionEntry(
            4,
            "Imprimir reportes",
            PermissionCategory.Print
        )
        var AddAssetReview = PermissionEntry(
            5,
            "Revisiones, agregar",
            PermissionCategory.AssetReview
        )
        var AddWarehouseMovement = PermissionEntry(
            6,
            "Movimientos, agregar ",
            PermissionCategory.Movement
        )
        private var AddUser = PermissionEntry(
            7,
            "Usuarios, agregar",
            PermissionCategory.User
        )
        private var ModifyUser = PermissionEntry(
            8,
            "Usuarios, modificar",
            PermissionCategory.User
        )
        var AddWarehouse = PermissionEntry(
            9,
            "Depósitos, agregar",
            PermissionCategory.Warehouse
        )
        var ModifyWarehouse = PermissionEntry(
            10,
            "Depósitos, modificar",
            PermissionCategory.Warehouse
        )
        var AddItemCategory = PermissionEntry(
            11,
            "Categorías, agregar ",
            PermissionCategory.ItemCategory
        )
        var ModifyItemCategory = PermissionEntry(
            12,
            "Categorías, modificar",
            PermissionCategory.ItemCategory
        )
        private var ModifyUserPermission = PermissionEntry(
            13,
            "Permisos, modificar",
            PermissionCategory.User
        )
        var UseCollectorProgram = PermissionEntry(
            14,
            "Programa del colector, control",
            PermissionCategory.Configuration
        )
        private var UseDesktopProgram = PermissionEntry(
            15,
            "Programa de escritorio, control",
            PermissionCategory.Configuration
        )
        private var ConfigureProgram = PermissionEntry(
            16,
            "Programa de escritorio, configurar",
            PermissionCategory.Configuration
        )
        private var AddProvider = PermissionEntry(
            17,
            "Proveedores, agregar",
            PermissionCategory.Entity
        )
        private var ModifyProvider = PermissionEntry(
            18,
            "Proveedores, modificar",
            PermissionCategory.Entity
        )
        private var AddCostCentre = PermissionEntry(
            19,
            "Centro de costos, agregar ",
            PermissionCategory.CostCentre
        )
        private var ModifyCostCentre = PermissionEntry(
            20,
            "Centro de costos, modificar",
            PermissionCategory.CostCentre
        )
        private var AddAttribute = PermissionEntry(
            21,
            "Atributos, agregar",
            PermissionCategory.Attribute
        )
        private var ModifyAttribute = PermissionEntry(
            22,
            "Atributos, modificar",
            PermissionCategory.Attribute
        )
        private var AddAttributeCategory = PermissionEntry(
            23,
            "Categoría de atributos, agregar",
            PermissionCategory.Attribute
        )
        private var ModifyAttributeCategory = PermissionEntry(
            24,
            "Categoría de atributos, modificar",
            PermissionCategory.Attribute
        )
        private var AddRoute = PermissionEntry(
            25,
            "Rutas, agregar",
            PermissionCategory.Route
        )
        private var ModifyRoute = PermissionEntry(
            26,
            "Rutas, modificar",
            PermissionCategory.Route
        )
        private var AddDataCollectionRule = PermissionEntry(
            27,
            "Reglas de recolección de datos, agregar",
            PermissionCategory.DataCollectionRule
        )
        private var ModifyDataCollectionRule = PermissionEntry(
            28,
            "Reglas de recolección de datos, modificar",
            PermissionCategory.DataCollectionRule
        )
        private var AddDataCollectionReport = PermissionEntry(
            29,
            "Reporte de recolección de datos, agregar",
            PermissionCategory.DataCollectionReport
        )
        private var ModifyDataCollectionReport = PermissionEntry(
            30,
            "Reporte de recolección de datos, modificar",
            PermissionCategory.DataCollectionReport
        )
        var ShowPreviousDataCollectionRegistry = PermissionEntry(
            31,
            "Colector, recolección de datos, mostrar registros antiguos",
            PermissionCategory.DataCollectionRule
        )
        var ReentryRouteProcessContent = PermissionEntry(
            32,
            "Colector, reingresar contenidos de ruta procesados",
            PermissionCategory.RouteProcess
        )

        ////////////// MENU
        private var DeskMenuAsset = PermissionEntry(
            50,
            "Menú, activos fijos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetManager = PermissionEntry(
            51,
            "Menú, activos fijos, administrador de activos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetLabelPrint = PermissionEntry(
            52,
            "Menú, activos fijos, imprimir etiquetas o rótulos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetMonitoring = PermissionEntry(
            53,
            "Menú, activos fijos, seguimiento de activos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetManteinanceProgramed = PermissionEntry(
            54,
            "Menú, activos fijos, tareas de mantenimiento programado",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetAmortization = PermissionEntry(
            55,
            "Menú, activos fijos, reporte de amortización",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetLease = PermissionEntry(
            56,
            "Menú, activos fijos, reporte de comodato",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAssetRental = PermissionEntry(
            57,
            "Menú, activos fijos, reporte de alquiler",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuItemCategory = PermissionEntry(
            58,
            "Menú, categorías",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuWarehouse = PermissionEntry(
            59,
            "Menú, depósitos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuProvider = PermissionEntry(
            60,
            "Menú, proveedores",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuUser = PermissionEntry(
            61,
            "Menú, usuarios",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuCostCentre = PermissionEntry(
            62,
            "Menú, centro de costos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuAttribute = PermissionEntry(
            63,
            "Menú, atributos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuDataCollection = PermissionEntry(
            64,
            "Menú, recolección de datos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuDataCollectionRule = PermissionEntry(
            65,
            "Menú, recolección de datos, reglas de recolección de datos",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuRoute = PermissionEntry(
            66,
            "Menú, recolección de datos, rutas",
            PermissionCategory.DeskMenu
        )
        private var DeskMenuDataCollectionReport = PermissionEntry(
            67,
            "Menú, recolección de datos, reporte de recolección de datos",
            PermissionCategory.DeskMenu
        )

        ////////////// PAGINAS
        private var DeskPageAssetReview = PermissionEntry(
            100,
            "Página, revisión",
            PermissionCategory.DeskPage
        )
        private var DeskPageMovement = PermissionEntry(
            101,
            "Página, movimiento",
            PermissionCategory.DeskPage
        )
        private var DeskPageAssetMonitoring = PermissionEntry(
            102,
            "Página, seguimiento de activos",
            PermissionCategory.DeskPage
        )
        private var DeskPageActionLog = PermissionEntry(
            103,
            "Página, registro de acciones",
            PermissionCategory.DeskPage
        )
        private var DeskPageImageControl = PermissionEntry(
            104,
            "Página, imágenes",
            PermissionCategory.DeskPage
        )
        private var DeskPageDataCollection = PermissionEntry(
            105,
            "Página, recolección de datos",
            PermissionCategory.DeskPage
        )
        private var DeskPageRouteProcess = PermissionEntry(
            106,
            "Página, proceso de ruta",
            PermissionCategory.DeskPage
        )
        private var DeskPageDataCollectionReport = PermissionEntry(
            107,
            "Página, reporte de recolección de datos",
            PermissionCategory.DeskPage
        )

        ////////////// ASSET MANTEINANCE
        private var SeeAllRepairshop = PermissionEntry(
            150,
            "Taller de mantenimiento, ver todos",
            PermissionCategory.AssetManteinance
        )
        private var AddAssetManteinance = PermissionEntry(
            151,
            "Mantenimiento programado, agregar",
            PermissionCategory.AssetManteinance
        )
        private var AddManteinanceTypeGroup = PermissionEntry(
            152,
            "Grupo de tareas, agregar",
            PermissionCategory.AssetManteinance
        )
        private var ModifyManteinanceTypeGroup = PermissionEntry(
            153,
            "Grupo de tareas, modificar",
            PermissionCategory.AssetManteinance
        )
        private var AssignRepairmanAndRepairshop = PermissionEntry(
            156,
            "Usuario y taller de mantenimiento, asignar",
            PermissionCategory.AssetManteinance
        )
        private var ModifyAssetManteinance = PermissionEntry(
            157,
            "Mantenimiento programado, modificar",
            PermissionCategory.AssetManteinance
        )
        private var AddAssetReception = PermissionEntry(
            158,
            "Activos, agregar recepción",
            PermissionCategory.AssetManteinance
        )
        private var AddAssetRemission = PermissionEntry(
            159,
            "Activos, agregar envío",
            PermissionCategory.AssetManteinance
        )
        private var AddRepairshop = PermissionEntry(
            160,
            "Taller de mantenimiento, agregar",
            PermissionCategory.AssetManteinance
        )
        private var ModifyRepairshop = PermissionEntry(
            161,
            "Taller de mantenimiento, modificar",
            PermissionCategory.AssetManteinance
        )
        private var SeeAsset = PermissionEntry(
            162,
            "Activos, mostrar",
            PermissionCategory.AssetManteinance
        )
        private var AddRepairman = PermissionEntry(
            163,
            "Empleados, agregar",
            PermissionCategory.Repairman
        )
        private var ModifyRepairman = PermissionEntry(
            164,
            "Empleados, modificar",
            PermissionCategory.Repairman
        )
        private var ModifyRepairmanPermission = PermissionEntry(
            165,
            "Permisos, modificar",
            PermissionCategory.Repairman
        )
        private var UseDesktopProgramAcm = PermissionEntry(
            166,
            "Programa de escritorio, control",
            PermissionCategory.Configuration
        )

        ////////////// COLECTOR
        var CollButtonAssetReview = PermissionEntry(
            200,
            "Botón del colector, revisión",
            PermissionCategory.CollMenu
        )
        var CollButtonAssetMovement = PermissionEntry(
            201,
            "Botón del colector, movimiento",
            PermissionCategory.CollMenu
        )
        var CollButtonSendAndDownload = PermissionEntry(
            202,
            "Botón del colector, enviar y recibir datos",
            PermissionCategory.CollMenu
        )
        var CollButtonWhatIs = PermissionEntry(
            203,
            "Botón del colector, ¿qué es?",
            PermissionCategory.CollMenu
        )
        var CollButtonCheckCode = PermissionEntry(
            204,
            "Botón del colector, lectura de códigos",
            PermissionCategory.CollMenu
        )
        var CollButtonRfidLink = PermissionEntry(
            205,
            "Botón del colector, vincular tags RFID",
            PermissionCategory.CollMenu
        )
        var CollButtonPrintLabel = PermissionEntry(
            206,
            "Botón del colector, imprimir etiquetas",
            PermissionCategory.CollMenu
        )

        // 207

        var CollButtonConfiguration = PermissionEntry(
            208,
            "Botón del colector, configuración",
            PermissionCategory.CollMenu
        )
        var CollButtonCRUD = PermissionEntry(
            209,
            "Botón del colector, altas y modificaciones",
            PermissionCategory.CollMenu
        )
        var CollButtonAssetManteinance = PermissionEntry(
            210,
            "Botón del colector, mantenimiento de activo",
            PermissionCategory.CollMenu
        )
        var CollButtonRoute = PermissionEntry(
            211,
            "Botón del colector, rutas",
            PermissionCategory.CollMenu
        )
        var CollButtonDataCollection = PermissionEntry(
            212,
            "Botón del colector, recolección de datos",
            PermissionCategory.CollMenu
        )

        ////////////// MENU ASSET MANTEINANCE
        private var DeskMenuInOut = PermissionEntry(
            300,
            "Menú, entradas / salidas",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuAssetRemission = PermissionEntry(
            301,
            "Menú, entradas / salidas, remisión de activos",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuAssetReception = PermissionEntry(
            302,
            "Menú, entradas / salidas, recepción de activos",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuAssetInGeneral = PermissionEntry(
            303,
            "Menú, activos en general",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuRepairshop = PermissionEntry(
            304,
            "Menú, talleres de mantenimiento",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuManteinanceType = PermissionEntry(
            305,
            "Menú, tareas",
            PermissionCategory.DeskMenuAssetManteinance
        )
        private var DeskMenuRepairman = PermissionEntry(
            306,
            "Menú, empleados de mantenimiento",
            PermissionCategory.DeskMenuAssetManteinance
        )

        ////////////// PAGINAS ASSET MANTEINANCE
        private var DeskPageAssetManteinance = PermissionEntry(
            350,
            "Página, mantenimiento",
            PermissionCategory.DeskPageAssetManteinance
        )
        private var DeskPageAssetWithManteinanceProgramed = PermissionEntry(
            351,
            "Página, activos en áreas de mantenimiento",
            PermissionCategory.DeskPageAssetManteinance
        )
        private var DeskPageAssetOnRepairshop = PermissionEntry(
            352,
            "Página, mantenimiento programado",
            PermissionCategory.DeskPageAssetManteinance
        )
        private var DeskPageActionLogAssetManteinance = PermissionEntry(
            353,
            "Página, registro de acciones",
            PermissionCategory.DeskPageAssetManteinance
        )
        private var DeskPageImageControlAssetManteinance = PermissionEntry(
            354,
            "Página, imágenes",
            PermissionCategory.DeskPageAssetManteinance
        )

        fun getAll(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            allSections.addAll(getAllAssetControlDesktop())
            allSections.addAll(getAllAssetControlSpecial())
            allSections.addAll(getAllAssetControlCollector())
            allSections.addAll(getAllAssetControlManteinanceDesktop())
            allSections.addAll(getAllAssetControlManteinanceSpecial())

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlDesktop(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            allSections.addAll(getAllAssetControlFunction())
            allSections.addAll(getAllAssetControlDeskMenu())
            allSections.addAll(getAllAssetControlDeskPage())

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlSpecial(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                UseCollectorProgram,
                UseDesktopProgram,
                ModifyUserPermission
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlFunction(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                AddAsset,
                ModifyAsset,
                AddUser,
                ModifyUser,
                AddWarehouse,
                ModifyWarehouse,
                AddItemCategory,
                ModifyItemCategory,
                AddCostCentre,
                ModifyCostCentre,
                AddProvider,
                ModifyProvider,
                AddManteinanceTypeGroup,
                ModifyManteinanceTypeGroup,
                PrintLabel,
                PrintReport,
                AddAssetReview,
                AddWarehouseMovement,
                AddAssetManteinance,
                SeeAllRepairshop,
                ConfigureProgram,
                AddAttribute,
                ModifyAttribute,
                AddAttributeCategory,
                ModifyAttributeCategory,
                AddRoute,
                ModifyRoute,
                AddDataCollectionRule,
                ModifyDataCollectionRule,
                AddDataCollectionReport,
                ModifyDataCollectionReport,
                ShowPreviousDataCollectionRegistry,
                ReentryRouteProcessContent
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlDeskPage(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                DeskPageActionLog,
                DeskPageAssetMonitoring,
                DeskPageAssetReview,
                DeskPageMovement,
                DeskPageImageControl,
                DeskPageDataCollection,
                DeskPageRouteProcess,
                DeskPageDataCollectionReport
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlDeskMenu(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                DeskMenuAsset,
                DeskMenuAssetAmortization,
                DeskMenuAssetLabelPrint,
                DeskMenuAssetLease,
                DeskMenuAssetManager,
                DeskMenuAssetManteinanceProgramed,
                DeskMenuAssetMonitoring,
                DeskMenuAssetRental,
                DeskMenuItemCategory,
                DeskMenuCostCentre,
                DeskMenuProvider,
                DeskMenuUser,
                DeskMenuWarehouse,
                DeskMenuAttribute,
                DeskMenuRoute,
                DeskMenuDataCollectionRule,
                DeskMenuDataCollectionReport,
                DeskMenuDataCollection
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlCollector(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                CollButtonAssetMovement,
                CollButtonAssetReview,
                CollButtonCheckCode,
                CollButtonConfiguration,
                CollButtonRfidLink,
                CollButtonSendAndDownload,
                CollButtonWhatIs,
                CollButtonRoute,
                CollButtonDataCollection,
                CollButtonAssetManteinance,
                CollButtonCRUD,
                CollButtonPrintLabel
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlManteinanceDesktop(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            allSections.addAll(getAllAssetControlManteinanceFunction())
            allSections.addAll(getAllAssetControlManteinanceDeskMenu())
            allSections.addAll(getAllAssetControlManteinanceDeskPage())

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlManteinanceFunction(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                AddRepairman,
                ModifyRepairman,
                AssignRepairmanAndRepairshop,
                ModifyAssetManteinance,
                AddAssetReception,
                AddAssetRemission,
                AddRepairshop,
                ModifyRepairshop,
                SeeAsset
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlManteinanceDeskMenu(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                DeskMenuRepairman,
                DeskMenuInOut,
                DeskMenuAssetRemission,
                DeskMenuAssetReception,
                DeskMenuAssetInGeneral,
                DeskMenuRepairshop,
                DeskMenuManteinanceType,
                DeskMenuRepairman
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlManteinanceDeskPage(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                DeskPageAssetManteinance,
                DeskPageAssetWithManteinanceProgramed,
                DeskPageAssetOnRepairshop,
                DeskPageActionLogAssetManteinance,
                DeskPageImageControlAssetManteinance
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        private fun getAllAssetControlManteinanceSpecial(): ArrayList<PermissionEntry> {
            val allSections = ArrayList<PermissionEntry>()
            Collections.addAll(
                allSections,
                UseDesktopProgramAcm,
                ModifyRepairmanPermission
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(permissionId: Long): PermissionEntry? {
            return getAll().firstOrNull { it.id == permissionId }
        }
    }
}