package com.dacosys.assetControl.model.reviews.async

import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.reviews.assetReviewContent.`object`.AssetReviewContent
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentDbHelper.OnInsertListener
import com.dacosys.assetControl.model.reviews.assetReviewContentStatus.AssetReviewContentStatus
import com.dacosys.assetControl.sync.functions.ProgressStatus
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class SaveReview {
    interface SaveReviewListener {
        // Define data you like to return from AysncTask
        fun onSaveReviewProgress(
            msg: String,
            taskStatus: Int,
            assetReview: AssetReview?,
            arContArray: ArrayList<AssetReviewContent>,
            progress: Int? = null,
            total: Int? = null,
        )
    }

    private var weakRef: WeakReference<SaveReviewListener>? = null
    private var saveReviewListener: SaveReviewListener?
        get() {
            return weakRef?.get()
        }
        set(value) {
            weakRef = if (value != null) WeakReference(value) else null
        }

    private var weakRef2: WeakReference<OnInsertListener>? = null
    private var insertListener: OnInsertListener?
        get() {
            return weakRef2?.get()
        }
        set(value) {
            weakRef2 = if (value != null) WeakReference(value) else null
        }

    private var assetReview: AssetReview? = null
    private var isNew = false
    private var lastId = 0L

    private var arcArray: ArrayList<AssetReviewContent> = ArrayList()
    private var msg = "Ok"

    fun addParams(
        saveReviewListener: SaveReviewListener,
        onInsertListener: OnInsertListener,
        assetReview: AssetReview,
        isNew: Boolean,
        lastCollectorId: Long,
    ) {
        // list all the parameters like in normal class define
        this.saveReviewListener = saveReviewListener
        this.insertListener = onInsertListener
        this.assetReview = assetReview
        this.isNew = isNew
        lastId = lastCollectorId
    }

    fun execute() {
        doInBackground()
    }

    private var job: Job? = null

    private fun doInBackground() {
        runBlocking {
            var result = false
            job = launch { result = suspendFunction() }
            job?.join()

            if (result) {
                saveReviewListener?.onSaveReviewProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.finished.id,
                    assetReview = assetReview,
                    arContArray = arcArray,
                    progress = 0,
                    total = 0
                )
            } else {
                saveReviewListener?.onSaveReviewProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.crashed.id,
                    assetReview = assetReview,
                    arContArray = arcArray,
                    progress = 0,
                    total = 0
                )
            }
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        var error = false

        try {
            saveReviewListener?.onSaveReviewProgress(
                msg = Statics.AssetControl.getContext().getString(R.string.getting_asset_in_area),
                taskStatus = ProgressStatus.starting.id,
                assetReview = assetReview,
                arContArray = arcArray,
                progress = 0,
                total = 0
            )

            if (isNew) {
                val allAsset = AssetDbHelper()
                    .selectByWarehouseAreaIdActiveNotRemoved(assetReview!!.warehouseAreaId)

                if (allAsset.size > 0) {
                    val total = allAsset.size
                    for ((p, asset) in allAsset.withIndex()) {
                        saveReviewListener?.onSaveReviewProgress(
                            msg = String.format(
                                Statics.AssetControl.getContext()
                                    .getString(R.string.loading_asset_), asset.code
                            ),
                            taskStatus = ProgressStatus.running.id,
                            assetReview = assetReview,
                            arContArray = arcArray,
                            progress = p,
                            total = total
                        )

                        lastId--

                        val newArCont = AssetReviewContent()

                        newArCont.assetId = asset.assetId
                        newArCont.contentStatusId = AssetReviewContentStatus.notInReview.id
                        newArCont.code = asset.code
                        newArCont.collectorContentId = lastId
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
                        insertListener!!,
                        assetReview!!,
                        arcArray.toTypedArray()
                    )
                }
            } else {
                val oldCont = assetReview!!.contents
                if (oldCont.size > 0) {
                    val total = oldCont.size
                    for ((p, asset) in oldCont.withIndex()) {
                        saveReviewListener?.onSaveReviewProgress(
                            msg = String.format(
                                Statics.AssetControl.getContext()
                                    .getString(R.string.loading_asset_), asset.code
                            ),
                            taskStatus = ProgressStatus.running.id,
                            assetReview = assetReview,
                            arContArray = arcArray,
                            progress = p,
                            total = total
                        )

                        lastId--
                        val newArCont = AssetReviewContent()

                        newArCont.assetId = asset.assetId
                        newArCont.assetStatusId = asset.assetStatusId
                        newArCont.labelNumber = asset.labelNumber
                        newArCont.contentStatusId = asset.contentStatusId
                        newArCont.code = asset.code
                        newArCont.collectorContentId = lastId
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