package com.dacosys.assetControl.model.category

import com.dacosys.assetControl.dataBase.category.ItemCategoryDbHelper
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.category.ItemCategoryObject
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class ItemCategoryCRUD {
    companion object {
        class ItemCategoryCRUDResult(var resultCode: Int, var itemCategory: ItemCategory?)

        /**
         * Interface que recibirá el resultado de las tareas asincrónicas
         */
        interface TaskCompleted {
            // Define data you like to return from AysncTask
            fun onTaskCompleted(result: ItemCategoryCRUDResult)
        }

        const val RC_UPDATE_OK = 1001
        const val RC_ERROR_UPDATE = 2001
        const val RC_ERROR_OBJECT_NULL = 3001
        const val RC_INSERT_OK = 4001
        const val RC_ERROR_INSERT = 5001
    }

    class ItemCategoryAdd {
        private var mCallback: TaskCompleted? = null
        private var itemCategoryObject: ItemCategoryObject? = null
        private var icCRUDResult = ItemCategoryCRUDResult(RC_ERROR_INSERT, null)

        fun addParams(callback: TaskCompleted, ItemCategoryObject: ItemCategoryObject) {
            // list all the parameters like in normal class define
            this.itemCategoryObject = ItemCategoryObject
            this.mCallback = callback
        }

        private val scope = CoroutineScope(Job() + Dispatchers.IO)

        fun cancel() {
            scope.cancel()
        }

        fun execute() {
            scope.launch { doInBackground() }
        }

        private suspend fun doInBackground() {
            coroutineScope {
                suspendFunction()
                mCallback?.onTaskCompleted(icCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (itemCategoryObject != null) {
                val icObj = itemCategoryObject!!
                if (addItemCategory(icObj).resultCode == RC_INSERT_OK) {
                    if (Statics.autoSend()) {
                        SyncUpload(SyncRegistryType.ItemCategory)
                    }
                } else {
                    icCRUDResult.resultCode = RC_ERROR_INSERT
                    icCRUDResult.itemCategory = null
                }
            }
        }

        private fun addItemCategory(icObj: ItemCategoryObject): ItemCategoryCRUDResult {
            var description = icObj.description
            if (icObj.description.length > 255) {
                description = icObj.description.substring(0, 255)
            }

            val newId = ItemCategoryDbHelper().minId
            if (ItemCategoryDbHelper().insert(
                    newId,
                    description,
                    icObj.active == 1,
                    icObj.parent_id,
                    false
                )
            ) {
                val ic = ItemCategory(icObj)
                ic.itemCategoryId = newId

                icCRUDResult.resultCode = RC_INSERT_OK
                icCRUDResult.itemCategory = ic
            } else {
                icCRUDResult.resultCode = RC_ERROR_INSERT
                icCRUDResult.itemCategory = null
            }

            return icCRUDResult
        }
    }

    class ItemCategoryUpdate {
        private var mCallback: TaskCompleted? = null
        private var itemCategoryObject: ItemCategoryObject? = null
        private var icCRUDResult = ItemCategoryCRUDResult(RC_ERROR_UPDATE, null)

        fun addParams(callback: TaskCompleted, itemCategoryObject: ItemCategoryObject) {
            // list all the parameters like in normal class define
            this.itemCategoryObject = itemCategoryObject
            this.mCallback = callback
        }

        private val scope = CoroutineScope(Job() + Dispatchers.IO)

        fun cancel() {
            scope.cancel()
        }

        fun execute() {
            scope.launch { doInBackground() }
        }

        private suspend fun doInBackground() {
            coroutineScope {
                suspendFunction()
                mCallback?.onTaskCompleted(icCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (itemCategoryObject != null) {
                val icObj = itemCategoryObject!!
                if (updateItemCategory(icObj).resultCode == RC_UPDATE_OK) {
                    if (Statics.autoSend()) {
                        SyncUpload(SyncRegistryType.ItemCategory)
                    }
                }
            } else {
                icCRUDResult.resultCode = RC_ERROR_OBJECT_NULL
                icCRUDResult.itemCategory = null
            }
        }

        private fun updateItemCategory(icObj: ItemCategoryObject): ItemCategoryCRUDResult {
            if (ItemCategoryDbHelper().update(icObj)) {
                icCRUDResult.resultCode = RC_UPDATE_OK
                icCRUDResult.itemCategory = ItemCategory(icObj)
            } else {
                icCRUDResult.resultCode = RC_ERROR_UPDATE
                icCRUDResult.itemCategory = null
            }

            return icCRUDResult
        }
    }
}