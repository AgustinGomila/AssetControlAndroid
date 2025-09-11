package com.example.assetControl.data.crud.category

import com.example.assetControl.data.enums.common.CrudCompleted
import com.example.assetControl.data.enums.common.CrudResult
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_INSERT
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_OBJECT_NULL
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_UPDATE
import com.example.assetControl.data.enums.common.CrudStatus.INSERT_OK
import com.example.assetControl.data.enums.common.CrudStatus.UPDATE_OK
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.repository.category.ItemCategoryRepository
import com.example.assetControl.data.webservice.category.ItemCategoryObject
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.sync.SyncUpload
import com.example.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val ic = ItemCategory(
                description = icObj.description.take(255),
                mActive = icObj.active,
                parentId = icObj.parent_id,
                transferred = 0
            )
            ItemCategoryRepository().insert(ic)

            crudResult.status = INSERT_OK
            crudResult.itemResult = ic

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
            val tempIc = ItemCategory(icObj)
            ItemCategoryRepository().update(tempIc)

            val ic = ItemCategoryRepository().selectById(tempIc.id)
            crudResult.status = UPDATE_OK
            crudResult.itemResult = ic

            return crudResult
        }
    }
}