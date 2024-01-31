package com.dacosys.assetControl.data.dataBase.attribute

import android.provider.BaseColumns

/**
 * Created by Agustin on 28/12/2016.
 */

object AttributeCompositionContract {
    fun getAllColumns(): Array<String> {
        return arrayOf(
            AttributeCompositionEntry.ATTRIBUTE_COMPOSITION_ID,
            AttributeCompositionEntry.DESCRIPTION,
            AttributeCompositionEntry.ATTRIBUTE_ID,
            AttributeCompositionEntry.ATTRIBUTE_COMPOSITION_TYPE_ID,
            AttributeCompositionEntry.COMPOSITION,
            AttributeCompositionEntry.USED,
            AttributeCompositionEntry.NAME,
            AttributeCompositionEntry.READ_ONLY,
            AttributeCompositionEntry.DEFAULT_VALUE
        )
    }

    abstract class AttributeCompositionEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "attribute_composition"

            const val ATTRIBUTE_COMPOSITION_ID = "_id"
            const val ATTRIBUTE_ID = "attribute_id"
            const val ATTRIBUTE_COMPOSITION_TYPE_ID = "attribute_composition_type_id"
            const val DESCRIPTION = "description"
            const val COMPOSITION = "composition"
            const val USED = "used"
            const val NAME = "name"
            const val READ_ONLY = "read_only"
            const val DEFAULT_VALUE = "default_value"
        }
    }
}
