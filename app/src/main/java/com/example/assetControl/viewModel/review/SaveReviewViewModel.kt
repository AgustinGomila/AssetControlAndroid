package com.example.assetControl.viewModel.review

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.review.StartReviewProgress

class SaveReviewViewModel : ViewModel() {
    val saveProgress: MutableLiveData<SaveProgress?> = MutableLiveData()
    val startReviewProgress: MutableLiveData<StartReviewProgress?> = MutableLiveData()

    fun getSaveProgress(): SaveProgress? {
        return saveProgress.value
    }

    fun setSaveProgress(it: SaveProgress) {
        saveProgress.postValue(it)
    }

    fun getStartReviewProgress(): StartReviewProgress? {
        return startReviewProgress.value
    }

    fun setStartReviewProgress(it: StartReviewProgress) {
        startReviewProgress.postValue(it)
    }
}

