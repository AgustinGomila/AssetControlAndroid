package com.dacosys.assetControl.dataBase.category

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object ItemCategoryContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            ItemCategoryEntry.ITEM_CATEGORY_ID,
            ItemCategoryEntry.DESCRIPTION,
            ItemCategoryEntry.ACTIVE,
            ItemCategoryEntry.PARENT_ID,
            ItemCategoryEntry.TRANSFERRED
        )
    }

    abstract class ItemCategoryEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "item_category"

            const val ITEM_CATEGORY_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val PARENT_ID = "parent_id"
            const val PARENT_STR = "parent_str"
            const val TRANSFERRED = "transferred"
        }
    }
}
