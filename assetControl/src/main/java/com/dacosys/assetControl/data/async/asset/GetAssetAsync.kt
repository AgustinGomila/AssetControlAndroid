package com.dacosys.assetControl.data.async.asset

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.*

class GetAssetAsync {
    interface GetAssetAsyncListener {
        fun onGetAssetProgress(
            msg: String,
            progressStatus: ProgressStatus,
            completeList: ArrayList<Asset>,
        )
    }

    private var mCallback: GetAssetAsyncListener? = null
    private var code: String = ""
    private var itemCategory: ItemCategory? = null
    private var warehouseArea: WarehouseArea? = null
    private var onlyActive: Boolean = true

    private var completeList: ArrayList<Asset> = ArrayList()

    private fun preExecute() {
        mCallback?.onGetAssetProgress(
            "",
            ProgressStatus.starting,
            completeList
        )
    }

    fun addParams(callback: GetAssetAsyncListener) {
        this.mCallback = callback
    }

    fun addExtraParams(
        code: String,
        itemCategory: ItemCategory?,
        warehouseArea: WarehouseArea?,
        onlyActive: Boolean,
    ) {
        this.code = code
        this.itemCategory = itemCategory
        this.warehouseArea = warehouseArea
        this.onlyActive = onlyActive
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        preExecute()
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(): Boolean {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        try {
            getAssetList()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            mCallback?.onGetAssetProgress(
                ex.message.toString(),
                ProgressStatus.crashed,
                completeList
            )
            return@withContext false
        }
        return@withContext true
    }

    private fun getAssetList() {
        mCallback?.onGetAssetProgress(
            "",
            ProgressStatus.running,
            completeList
        )

        if (code.isEmpty() &&
            (itemCategory == null || (itemCategory?.id ?: -1) <= 0) &&
            (warehouseArea == null || (warehouseArea?.id ?: -1) <= 0)
        ) {
            mCallback?.onGetAssetProgress(
                getContext()
                    .getString(R.string.you_must_enter_at_least_one_letter_in_the_description_category_or_warehouse_area),
                ProgressStatus.canceled,
                completeList
            )
            return
        }

        try {
            completeList = ArrayList(
                AssetRepository().getBy(
                    code = code,
                    itemCategoryId = itemCategory?.id,
                    warehouseAreaId = warehouseArea?.id,
                    onlyActive = onlyActive
                )
            )
            mCallback?.onGetAssetProgress(
                "",
                ProgressStatus.finished,
                completeList
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            mCallback?.onGetAssetProgress(
                ex.message.toString(),
                ProgressStatus.crashed,
                completeList
            )
        }
    }
}