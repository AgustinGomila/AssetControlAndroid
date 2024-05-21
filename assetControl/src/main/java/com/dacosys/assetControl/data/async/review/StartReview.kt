package com.dacosys.assetControl.data.async.review

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.enums.review.AssetReviewContentStatus
import com.dacosys.assetControl.data.enums.review.StartReviewProgress
import com.dacosys.assetControl.data.room.dto.review.AssetReview
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContent
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.review.AssetReviewContentRepository
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
                    )
                )
            } else {
                onProgress.invoke(
                    StartReviewProgress(
                        msg = msg,
                        taskStatus = ProgressStatus.crashed.id,
                        assetReview = assetReview,
                        arContArray = arcArray,
                    )
                )
            }
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        var error = false
        val ar = assetReview ?: return@withContext false

        try {
            onProgress.invoke(
                StartReviewProgress(
                    msg = getContext().getString(R.string.getting_asset_in_area),
                    taskStatus = ProgressStatus.starting.id,
                    assetReview = assetReview,
                    arContArray = arcArray,
                )
            )

            if (isNew) {
                val allAsset = AssetRepository().selectByWarehouseAreaIdActiveNotRemoved(ar.warehouseAreaId)

                if (allAsset.isNotEmpty()) {
                    val total = allAsset.size
                    for ((p, asset) in allAsset.withIndex()) {
                        onProgress.invoke(
                            StartReviewProgress(
                                msg = String.format(
                                    getContext()
                                        .getString(R.string.loading_asset_), asset.code
                                ),
                                taskStatus = ProgressStatus.running.id,
                                assetReview = ar,
                                arContArray = arcArray,
                                progress = p,
                                total = total
                            )
                        )

                        lastId--
                        val newArCont = AssetReviewContent(
                            id = lastId,
                            contentStatusId = AssetReviewContentStatus.notInReview.id,
                            assetId = asset.id,
                            code = asset.code,
                            description = asset.description,
                            assetStatusId = asset.status,
                            warehouseAreaId = asset.warehouseAreaId,
                            labelNumber = asset.labelNumber ?: 0,
                            parentId = asset.parentId ?: 0,
                            warehouseAreaStr = asset.warehouseAreaStr,
                            warehouseStr = asset.warehouseStr,
                            itemCategoryId = asset.itemCategoryId,
                            itemCategoryStr = asset.itemCategoryStr,
                            ownershipStatusId = asset.ownershipStatus,
                            manufacturer = asset.manufacturer ?: "",
                            model = asset.model ?: "",
                            serialNumber = asset.serialNumber ?: "",
                            ean = asset.ean ?: "",
                        )
                        arcArray.add(newArCont)
                    }

                    AssetReviewContentRepository().insert(
                        review = ar,
                        contents = arcArray,
                        progress = onSaveProgress
                    )
                }
            } else {
                val oldCont = ar.contents()
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
                                assetReview = ar,
                                arContArray = arcArray,
                                progress = p,
                                total = total
                            )
                        )

                        lastId--
                        val newArCont = AssetReviewContent(
                            id = lastId,
                            assetId = asset.assetId,
                            assetStatusId = asset.assetStatusId,
                            labelNumber = asset.labelNumber,
                            contentStatusId = asset.contentStatusId,
                            code = asset.code,
                            description = asset.description,
                            warehouseAreaId = asset.originWarehouseAreaId,
                            parentId = asset.parentId,
                            warehouseAreaStr = asset.warehouseAreaStr,
                            warehouseStr = asset.warehouseStr,
                            itemCategoryId = asset.itemCategoryId,
                            itemCategoryStr = asset.itemCategoryStr,
                            ownershipStatusId = asset.ownershipStatusId,
                            manufacturer = asset.manufacturer,
                            model = asset.model,
                            serialNumber = asset.serialNumber,
                            ean = asset.ean
                        )
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