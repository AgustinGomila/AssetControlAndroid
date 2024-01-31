package com.dacosys.assetControl.data.model.category

import com.dacosys.assetControl.data.dataBase.category.ItemCategoryDbHelper
import com.dacosys.assetControl.data.model.common.CrudCompleted
import com.dacosys.assetControl.data.model.common.CrudResult
import com.dacosys.assetControl.data.model.common.CrudStatus.*
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class ItemCategoryCRUD {
    class ItemCategoryAdd {
        private var mCallback: CrudCompleted? = null
        private var itemCategoryObject: ItemCategoryObject? = null
        private var crudResult = CrudResult<ItemCategory?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, categoryObject: ItemCategoryObject) {
            // list all the parameters like in normal class define
            this.itemCategoryObject = categoryObject
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
                mCallback?.onCompleted(crudResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (itemCategoryObject != null) {
                val icObj = itemCategoryObject!!
                if (addItemCategory(icObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.ItemCategory)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addItemCategory(icObj: ItemCategoryObject): CrudResult<ItemCategory?> {
            var description = icObj.description
            if (icObj.description.length > 255) {
                description = icObj.description.substring(0, 255)
            }

            val newId = ItemCategoryDbHelper().minId
            if (ItemCategoryDbHelper().insert(
                    itemCategoryId = newId,
                    description = description,
                    active = icObj.active == 1,
                    parentId = icObj.parent_id,
                    transferred = false
                )
            ) {
                val ic = ItemCategory(icObj)
                ic.itemCategoryId = newId

                crudResult.status = INSERT_OK
                crudResult.itemResult = ic
            } else {
                crudResult.status = ERROR_INSERT
                crudResult.itemResult = null
            }

            return crudResult
        }
    }

    class ItemCategoryUpdate {
        private var mCallback: CrudCompleted? = null
        private var itemCategoryObject: ItemCategoryObject? = null
        private var crudResult = CrudResult<ItemCategory?>(ERROR_UPDATE, null)

        fun addParams(callback: CrudCompleted, itemCategoryObject: ItemCategoryObject) {
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
                mCallback?.onCompleted(crudResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (itemCategoryObject != null) {
                val icObj = itemCategoryObject!!
                if (updateItemCategory(icObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.ItemCategory)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateItemCategory(icObj: ItemCategoryObject): CrudResult<ItemCategory?> {
            if (ItemCategoryDbHelper().update(icObj)) {
                crudResult.status = UPDATE_OK
                crudResult.itemResult = ItemCategory(icObj)
            } else {
                crudResult.status = ERROR_UPDATE
                crudResult.itemResult = null
            }

            return crudResult
        }
    }
}