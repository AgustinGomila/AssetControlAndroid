package com.example.assetControl.data.crud.location

import com.example.assetControl.data.enums.common.CrudCompleted
import com.example.assetControl.data.enums.common.CrudResult
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_INSERT
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_OBJECT_NULL
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_UPDATE
import com.example.assetControl.data.enums.common.CrudStatus.INSERT_OK
import com.example.assetControl.data.enums.common.CrudStatus.UPDATE_OK
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.webservice.location.WarehouseAreaObject
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
            val tempWa = WarehouseArea(waObj)
            WarehouseAreaRepository().update(tempWa)

            val wa = WarehouseAreaRepository().selectById(tempWa.id)
            crudResult.status = UPDATE_OK
            crudResult.itemResult = wa

            return crudResult
        }
    }
}