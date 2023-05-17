package com.dacosys.assetControl.utils.mainButton

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.user.permission.PermissionEntry
import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

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
            getContext().getString(R.string.asset_reviews),
            R.drawable.ic_revision,
            PermissionEntry.CollButtonAssetReview
        )
        var AssetMovement = MainButton(
            2,
            getContext().getString(R.string.movements),
            R.drawable.ic_movement,
            PermissionEntry.CollButtonAssetMovement
        )
        var SendAndDownload = MainButton(
            3,
            getContext().getString(R.string.send_and_receive_data),
            R.drawable.ic_send,
            PermissionEntry.CollButtonSendAndDownload
        )
        var WhatIs = MainButton(
            4,
            getContext().getString(R.string.what_is_question),
            null,
            PermissionEntry.CollButtonWhatIs
        )
        var CheckCode = MainButton(
            5,
            getContext().getString(R.string.code_read),
            R.drawable.ic_what_is,
            PermissionEntry.CollButtonCheckCode
        )
        var RfidLink = MainButton(
            6,
            getContext().getString(R.string.link_RFID_tags),
            null,
            PermissionEntry.CollButtonRfidLink
        )
        var PrintLabel = MainButton(
            7,
            getContext().getString(R.string.assets_and_areas_catalog),
            R.drawable.ic_printer,
            PermissionEntry.CollButtonPrintLabel
        )
        var CRUD = MainButton(
            8,
            getContext().getString(R.string.registration_and_modification),
            R.drawable.ic_crud,
            PermissionEntry.CollButtonCRUD
        )
        var AssetManteinance = MainButton(
            9,
            getContext().getString(R.string.maintenances),
            null,
            PermissionEntry.CollButtonAssetManteinance
        )
        var Route = MainButton(
            10,
            getContext().getString(R.string.routes),
            R.drawable.ic_route,
            PermissionEntry.CollButtonRoute
        )
        var DataCollection = MainButton(
            11,
            getContext().getString(R.string.data_collection),
            R.drawable.ic_data_collection,
            PermissionEntry.CollButtonDataCollection
        )
        var Configuration = MainButton(
            100,
            getContext().getString(R.string.settings),
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
                AssetManteinance,
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
                AssetManteinance,
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