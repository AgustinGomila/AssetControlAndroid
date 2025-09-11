package com.example.assetControl.ui.adapters.interfaces

import com.example.assetControl.network.utils.ProgressStatus

class Interfaces {
    data class AdapterProgress(
        var totalTask: Int = 0,
        var completedTask: Int = 0,
        var msg: String = "",
        var progressStatus: ProgressStatus = ProgressStatus.unknown,
    )

    interface UiEventListener {
        fun onUiEventRequired(it: AdapterProgress)
    }

    interface DataSetChangedListener {
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        fun onCheckedChanged(isChecked: Boolean, pos: Int)
    }

    interface EditAssetRequiredListener {
        fun onEditAssetRequired(tableId: Int, itemId: Long)
    }

    interface AlbumViewRequiredListener {
        fun onAlbumViewRequired(tableId: Int, itemId: Long, filename: String = "")
    }

    interface AddPhotoRequiredListener {
        fun onAddPhotoRequired(
            tableId: Int,
            itemId: Long,
            description: String,
            obs: String = "",
            reference: String = ""
        )
    }
}