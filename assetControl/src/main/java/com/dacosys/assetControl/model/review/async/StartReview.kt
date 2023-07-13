package com.dacosys.assetControl.model.review.async

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.review.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.common.SaveProgress
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.model.review.AssetReviewContentStatus
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.*

class StartReview {
    private var assetReview: AssetReview? = null
    private var isNew = false
    private var lastId = 0L

    private var arcArray: ArrayList<AssetReviewContent> = ArrayList()
    private var msg = "Ok"
    private var onProgress: (StartReviewProgress) -> Unit = {}
    private var onSaveProgress: (SaveProgress) -> Unit = {}

    fun addParams(
        assetReview: AssetReview,
        isNew: Boolean,
        lastCollectorId: Long,
        onProgress: (StartReviewProgress) -> Unit = {},
        onSaveProgress: (SaveProgress) -> Unit = {},
    ) {
        this.onProgress = onProgress
        this.onSaveProgress = onSaveProgress
        this.assetReview = assetReview
        this.isNew = isNew
        lastId = lastCollectorId
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(): Boolean {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false

            if (result) {
                onProgress.invoke(
                    StartReviewProgress(
                        msg = msg,
                        taskStatus = ProgressStatus.finished.id,
                        assetReview = assetReview,
                        arContArray = arcArray,
                        progress = 0,
                        total = 0
                    )
                )
            } else {
                onProgress.invoke(
                    StartReviewProgress(
                        msg = msg,
                        taskStatus = ProgressStatus.crashed.id,
                        assetReview = assetReview,
                        arContArray = arcArray,
                        progress = 0,
                        total = 0
                    )
                )
            }
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        var error = false

        try {
            onProgress.invoke(
                StartReviewProgress(
                    msg = getContext().getString(R.string.getting_asset_in_area),
                    taskStatus = ProgressStatus.starting.id,
                    assetReview = assetReview,
                    arContArray = arcArray,
                    progress = 0,
                    total = 0
                )
            )

            if (isNew) {
                val allAsset = AssetDbHelper()
                    .selectByWarehouseAreaIdActiveNotRemoved(assetReview!!.warehouseAreaId)

                if (allAsset.size > 0) {
                    val total = allAsset.size
                    for ((p, asset) in allAsset.withIndex()) {
                        onProgress.invoke(
                            StartReviewProgress(
                                msg = String.format(
                                    getContext()
                                        .getString(R.string.loading_asset_), asset.code
                                ),
                                taskStatus = ProgressStatus.running.id,
                                assetReview = assetReview,
                                arContArray = arcArray,
                                progress = p,
                                total = total
                            )
                        )

                        lastId--

                        val newArCont = AssetReviewContent()

                        newArCont.assetReviewContentId = lastId
                        newArCont.contentStatusId = AssetReviewContentStatus.notInReview.id
                        newArCont.assetId = asset.assetId
                        newArCont.code = asset.code
                        newArCont.description = asset.description
                        newArCont.assetStatusId = asset.assetStatusId
                        newArCont.warehouseAreaId = asset.warehouseAreaId
                        newArCont.labelNumber = asset.labelNumber ?: 0
                        newArCont.parentId = asset.parentAssetId ?: 0
                        newArCont.warehouseAreaStr = asset.warehouseAreaStr
                        newArCont.warehouseStr = asset.warehouseStr
                        newArCont.itemCategoryId = asset.itemCategoryId
                        newArCont.itemCategoryStr = asset.itemCategoryStr
                        newArCont.ownershipStatusId = asset.ownershipStatusId
                        newArCont.manufacturer = asset.manufacturer ?: ""
                        newArCont.model = asset.model ?: ""
                        newArCont.serialNumber = asset.serialNumber ?: ""
                        newArCont.ean = asset.ean ?: ""

                        newArCont.setDataRead()
                        arcArray.add(newArCont)
                    }

                    AssetReviewContentDbHelper().insert(
                        assetReview!!,
                        arcArray.toTypedArray(),
                        onSaveProgress
                    )
                }
            } else {
                val oldCont = assetReview!!.contents
                if (oldCont.size > 0) {
                    val total = oldCont.size
                    for ((p, asset) in oldCont.withIndex()) {
                        onProgress.invoke(
                            StartReviewProgress(
                                msg = String.format(
                                    getContext()
                                        .getString(R.string.loading_asset_), asset.code
                                ),
                                taskStatus = ProgressStatus.running.id,
                                assetReview = assetReview,
                                arContArray = arcArray,
                                progress = p,
                                total = total
                            )
                        )

                        lastId--
                        val newArCont = AssetReviewContent()

                        newArCont.assetReviewContentId = lastId
                        newArCont.assetId = asset.assetId
                        newArCont.assetStatusId = asset.assetStatusId
                        newArCont.labelNumber = asset.labelNumber
                        newArCont.contentStatusId = asset.contentStatusId
                        newArCont.code = asset.code
                        newArCont.description = asset.description
                        newArCont.warehouseAreaId = asset.originWarehouseAreaId
                        newArCont.parentId = asset.parentId
                        newArCont.warehouseAreaStr = asset.warehouseAreaStr
                        newArCont.warehouseStr = asset.warehouseStr
                        newArCont.itemCategoryId = asset.itemCategoryId
                        newArCont.itemCategoryStr = asset.itemCategoryStr
                        newArCont.ownershipStatusId = asset.ownershipStatusId
                        newArCont.manufacturer = asset.manufacturer
                        newArCont.model = asset.model
                        newArCont.serialNumber = asset.serialNumber
                        newArCont.ean = asset.ean

                        newArCont.setDataRead()
                        arcArray.add(newArCont)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
            msg = ex.message.toString()
        }

        return@withContext !error
    }
}