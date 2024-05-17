package com.dacosys.assetControl.viewModel.review

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.enums.review.StartReviewProgress

class SaveReviewViewModel : ViewModel() {
    val saveProgress: MutableLiveData<SaveProgress?> = MutableLiveData()
    val startReviewProgress: MutableLiveData<StartReviewProgress?> = MutableLiveData()

    @Suppress("unused")
    fun getSaveProgress(): SaveProgress? {
        return saveProgress.value
    }

    fun setSaveProgress(it: SaveProgress) {
        saveProgress.postValue(it)
    }

    @Suppress("unused")
    fun getStartReviewProgress(): StartReviewProgress? {
        return startReviewProgress.value
    }

    fun setStartReviewProgress(it: StartReviewProgress) {
        startReviewProgress.postValue(it)
    }
}

