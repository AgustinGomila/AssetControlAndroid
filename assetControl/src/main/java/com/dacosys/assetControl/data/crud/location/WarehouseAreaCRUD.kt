package com.dacosys.assetControl.data.crud.location

import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.common.CrudResult
import com.dacosys.assetControl.data.enums.common.CrudStatus.*
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class WarehouseAreaCRUD {
    class WarehouseAreaAdd {
        private var mCallback: CrudCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var crudResult = CrudResult<WarehouseArea?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, areaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = areaObject
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
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (addWarehouseArea(waObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.WarehouseArea)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addWarehouseArea(waObj: WarehouseAreaObject): CrudResult<WarehouseArea?> {
            val wa = WarehouseArea(
                description = waObj.description.take(255),
                mActive = waObj.active,
                warehouseId = waObj.warehouse_id,
                transferred = 0
            )
            WarehouseAreaRepository().insert(wa)

            crudResult.status = INSERT_OK
            crudResult.itemResult = wa

            return crudResult
        }
    }

    class WarehouseAreaUpdate {
        private var mCallback: CrudCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var crudResult = CrudResult<WarehouseArea?>(ERROR_UPDATE, null)

        fun addParams(callback: CrudCompleted, warehouseAreaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = warehouseAreaObject
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
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (updateWarehouseArea(waObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.WarehouseArea)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateWarehouseArea(waObj: WarehouseAreaObject): CrudResult<WarehouseArea?> {
            val wa = WarehouseArea(waObj)

            if (WarehouseAreaRepository().update(wa)) {
                crudResult.status = UPDATE_OK
                crudResult.itemResult = wa
            } else {
                crudResult.status = ERROR_UPDATE
                crudResult.itemResult = null
            }

            return crudResult
        }
    }
}