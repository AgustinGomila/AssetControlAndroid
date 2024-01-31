package com.dacosys.assetControl.ui.activities.category

import com.dacosys.assetControl.data.model.category.ItemCategory

interface ItemCategoryChangedObserver {
    fun onItemCategoryChanged(w: ItemCategory?)
}