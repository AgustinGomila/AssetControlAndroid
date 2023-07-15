package com.dacosys.assetControl.adapters.interfaces

import com.dacosys.assetControl.adapters.review.ArcRecyclerAdapter

class Interfaces {
    interface UiEventListener {
        fun onUiEventRequired(it: ArcRecyclerAdapter.AdapterProgress)
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
        fun onAlbumViewRequired(tableId: Int, itemId: Long)
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