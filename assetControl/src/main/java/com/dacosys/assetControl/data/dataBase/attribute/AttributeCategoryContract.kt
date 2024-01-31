package com.dacosys.assetControl.data.dataBase.attribute

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AttributeCategoryContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AttributeCategoryEntry.ATTRIBUTE_CATEGORY_ID,
            AttributeCategoryEntry.DESCRIPTION,
            AttributeCategoryEntry.ACTIVE,
            AttributeCategoryEntry.PARENT_ID
        )
    }

    abstract class AttributeCategoryEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "attribute_category"

            const val ATTRIBUTE_CATEGORY_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val PARENT_ID = "parent_id"
        }
    }
}
