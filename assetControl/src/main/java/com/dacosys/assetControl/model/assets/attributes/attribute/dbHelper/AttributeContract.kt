package com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AttributeContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AttributeEntry.ATTRIBUTE_ID,
            AttributeEntry.DESCRIPTION,
            AttributeEntry.ACTIVE,
            AttributeEntry.ATTRIBUTE_TYPE_ID,
            AttributeEntry.ATTRIBUTE_CATEGORY_ID
        )
    }

    abstract class AttributeEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "attribute"

            const val ATTRIBUTE_ID = "_id"
            const val DESCRIPTION = "description"
            const val ACTIVE = "active"
            const val ATTRIBUTE_TYPE_ID = "attribute_type_id"
            const val ATTRIBUTE_CATEGORY_ID = "attribute_category_id"
        }
    }
}
