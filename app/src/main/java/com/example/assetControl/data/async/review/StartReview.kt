package com.example.assetControl.data.async.review

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.review.AssetReviewContentStatus
import com.example.assetControl.data.enums.review.StartReviewProgress
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartReview {
    private var assetReview: AssetReview? = null
    private var isNew = false

    private var arcArray: ArrayList<AssetReviewContent> = ArrayList()
    private var msg = "Ok"
    private var onProgress: (StartReviewProgress) -> Unit = {}
    private var onSaveProgress: (SaveProgress) -> Unit = {}

    fun addParams(
        assetReview: AssetReview,
        isNew: Boolean,
        onProgress: (StartReviewProgress) -> Unit = {},
        onSaveProgress: (SaveProgress) -> Unit = {},
    ) {
        this.onProgress = onProgress
        this.onSaveProgress = onSaveProgress
        this.assetReview = assetReview
        this.isNew = isNew
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
            result = deferred?.await() == true

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

        val contentRepository = AssetReviewContentRepository()
        val assetRepository = AssetRepository()

        try {
            onProgress.invoke(
                StartReviewProgress(
                    msg = context.getString(R.string.getting_asset_in_area),
                    taskStatus = ProgressStatus.starting.id,
                    assetReview = assetReview,
                    arContArray = arcArray,
                )
            )

            if (isNew) {
                val assets = assetRepository.selectByWarehouseAreaIdActiveNotRemoved(ar.warehouseAreaId)

                if (assets.isNotEmpty()) {

                    val total = assets.size
                    var lastId = contentRepository.maxId

                    for ((p, asset) in assets.withIndex()) {
                        onProgress.invoke(
                            StartReviewProgress(
                                msg = String.format(context.getString(R.string.loading_asset_), asset.code),
                                taskStatus = ProgressStatus.running.id,
                                assetReview = ar,
                                arContArray = arcArray,
                                progress = p,
                                total = total
                            )
                        )

                        lastId++
                        val newArCont = AssetReviewContent(
                            id = lastId,
                            contentStatusId = AssetReviewContentStatus.notInReview.id,
                            assetId = asset.id,
                            assetCode = asset.code,
                            assetDescription = asset.description,
                            assetStatusId = asset.status,
                            warehouseAreaId = asset.warehouseAreaId,
                            labelNumber = asset.labelNumber ?: 0,
                            parentId = asset.parentId ?: 0,
                            warehouseAreaDescription = asset.warehouseAreaStr,
                            warehouseDescription = asset.warehouseStr,
                            itemCategoryId = asset.itemCategoryId,
                            itemCategoryDescription = asset.itemCategoryStr,
                            ownershipStatusId = asset.ownershipStatus,
                            manufacturer = asset.manufacturer.orEmpty(),
                            model = asset.model.orEmpty(),
                            serialNumber = asset.serialNumber.orEmpty(),
                            ean = asset.ean.orEmpty(),
                        )
                        arcArray.add(newArCont)
                    }

                    contentRepository.insert(
                        review = ar,
                        contents = arcArray,
                        progress = onSaveProgress
                    )
                }
            } else {
                arcArray = ar.contents()
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