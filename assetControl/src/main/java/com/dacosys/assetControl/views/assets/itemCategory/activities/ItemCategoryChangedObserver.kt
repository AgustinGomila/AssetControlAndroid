package com.dacosys.assetControl.views.assets.itemCategory.activities

import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory

interface ItemCategoryChangedObserver {
    fun onItemCategoryChanged(w: ItemCategory?)
}