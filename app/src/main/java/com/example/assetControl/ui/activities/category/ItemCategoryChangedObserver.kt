package com.example.assetControl.ui.activities.category

import com.example.assetControl.data.room.dto.category.ItemCategory

interface ItemCategoryChangedObserver {
    fun onItemCategoryChanged(w: ItemCategory?)
}