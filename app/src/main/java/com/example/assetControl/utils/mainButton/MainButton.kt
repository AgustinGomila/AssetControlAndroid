package com.example.assetControl.utils.mainButton

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.permission.PermissionEntry
import java.util.*

class MainButton(
    mainButton: Long,
    description: String,
    iconResource: Int?,
    permissionEntry: PermissionEntry,
) {
    var mainButtonId: Long = 0
    var description: String = ""
    var iconResource: Int? = 0
    var permissionEntry: PermissionEntry? = null

    init {
        this.description = description
        this.mainButtonId = mainButton
        this.iconResource = iconResource
        this.permissionEntry = permissionEntry
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is MainButton) {
            false
        } else this.mainButtonId == other.mainButtonId
    }

    override fun hashCode(): Int {
        return this.mainButtonId.hashCode()
    }

    companion object {
        var AssetReview = MainButton(
            1,
            context.getString(R.string.asset_reviews),
            R.drawable.ic_revision,
            PermissionEntry.CollButtonAssetReview
        )
        var AssetMovement = MainButton(
            2,
            context.getString(R.string.movements),
            R.drawable.ic_movement,
            PermissionEntry.CollButtonAssetMovement
        )
        var SendAndDownload = MainButton(
            3,
            context.getString(R.string.send_and_receive_data),
            R.drawable.ic_send,
            PermissionEntry.CollButtonSendAndDownload
        )
        var WhatIs = MainButton(
            4,
            context.getString(R.string.what_is_question),
            null,
            PermissionEntry.CollButtonWhatIs
        )
        var CheckCode = MainButton(
            5,
            context.getString(R.string.code_read),
            R.drawable.ic_what_is,
            PermissionEntry.CollButtonCheckCode
        )
        var RfidLink = MainButton(
            6,
            context.getString(R.string.link_RFID_tags),
            null,
            PermissionEntry.CollButtonRfidLink
        )
        var PrintLabel = MainButton(
            7,
            context.getString(R.string.assets_and_areas_catalog),
            R.drawable.ic_printer,
            PermissionEntry.CollButtonPrintLabel
        )
        var CRUD = MainButton(
            8,
            context.getString(R.string.registration_and_modification),
            R.drawable.ic_crud,
            PermissionEntry.CollButtonCRUD
        )
        var AssetMaintenance = MainButton(
            9,
            context.getString(R.string.maintenances),
            null,
            PermissionEntry.CollButtonAssetMaintenance
        )
        var Route = MainButton(
            10,
            context.getString(R.string.routes),
            R.drawable.ic_route,
            PermissionEntry.CollButtonRoute
        )
        var DataCollection = MainButton(
            11,
            context.getString(R.string.data_collection),
            R.drawable.ic_data_collection,
            PermissionEntry.CollButtonDataCollection
        )
        var Configuration = MainButton(
            100,
            context.getString(R.string.settings),
            R.drawable.ic_settings,
            PermissionEntry.CollButtonConfiguration
        )

        fun getAll(): ArrayList<MainButton> {
            val allSections = ArrayList<MainButton>()
            Collections.addAll(
                allSections,
                AssetMovement,
                AssetReview,
                SendAndDownload,
                // WhatIs, <-- No se usa en esta versión para Android
                CheckCode,
                // RfidLink, <-- Está dentro del Asset CRUD
                CRUD,
                PrintLabel,
                AssetMaintenance,
                Route,
                DataCollection,
                Configuration
            )

            return ArrayList(allSections.sortedWith(compareBy { it.mainButtonId }))
        }

        fun getAllMain(): ArrayList<MainButton> {
            val allSections = ArrayList<MainButton>()

            Collections.addAll(
                allSections,
                AssetMovement,
                AssetReview,
                SendAndDownload,
                // WhatIs, <-- No se usa en esta versión para Android
                CheckCode,
                // RfidLink, <-- Está dentro del Asset CRUD
                CRUD,
                PrintLabel,
                AssetMaintenance,
                Route,
                DataCollection
            )

            return ArrayList(allSections.sortedWith(compareBy { it.mainButtonId }))
        }

        fun getById(mainButtonId: Long): MainButton? {
            return getAll().firstOrNull { it.mainButtonId == mainButtonId }
        }
    }
}