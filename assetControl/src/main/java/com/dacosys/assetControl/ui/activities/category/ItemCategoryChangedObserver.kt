package com.dacosys.assetControl.ui.activities.category

import com.dacosys.assetControl.data.room.entity.category.ItemCategory

interface ItemCategoryChangedObserver {
    fun onItemCategoryChanged(w: ItemCategory?)
}