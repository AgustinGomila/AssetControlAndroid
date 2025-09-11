package com.example.assetControl.data.crud.location

import com.example.assetControl.data.enums.common.CrudCompleted
import com.example.assetControl.data.enums.common.CrudResult
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_INSERT
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_OBJECT_NULL
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_UPDATE
import com.example.assetControl.data.enums.common.CrudStatus.INSERT_OK
import com.example.assetControl.data.enums.common.CrudStatus.UPDATE_OK
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.repository.location.WarehouseRepository
import com.example.assetControl.data.webservice.location.WarehouseObject
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
class WarehouseCRUD {
    class WarehouseAdd {
        private var mCallback: CrudCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var crudResult = CrudResult<Warehouse?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, warehouseObject: WarehouseObject) {
            // list all the parameters like in normal class define
            this.warehouseObject = warehouseObject
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
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (addWarehouse(wObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addWarehouse(wObj: WarehouseObject): CrudResult<Warehouse?> {
            val w = Warehouse(
                description = wObj.description.take(255),
                mActive = wObj.active,
                transferred = 0
            )
            WarehouseRepository().insert(w)

            crudResult.status = INSERT_OK
            crudResult.itemResult = w

            return crudResult
        }
    }

    class WarehouseUpdate {
        private var mCallback: CrudCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var crudResult = CrudResult<Warehouse?>(ERROR_UPDATE, null)

        fun addParams(callback: CrudCompleted, warehouseObject: WarehouseObject) {
            // list all the parameters like in normal class define
            this.warehouseObject = warehouseObject
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
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (updateWarehouse(wObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateWarehouse(wObj: WarehouseObject): CrudResult<Warehouse?> {
            val w = Warehouse(wObj)
            WarehouseRepository().update(w)
            crudResult.status = UPDATE_OK
            crudResult.itemResult = w

            return crudResult
        }
    }
}