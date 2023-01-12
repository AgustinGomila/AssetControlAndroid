package com.dacosys.assetControl.ui.activities.category

import com.dacosys.assetControl.model.category.ItemCategory

interface ItemCategoryChangedObserver {
    fun onItemCategoryChanged(w: ItemCategory?)
}