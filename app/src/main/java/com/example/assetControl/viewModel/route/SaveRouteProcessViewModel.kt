package com.example.assetControl.viewModel.route

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.assetControl.data.enums.common.SaveProgress

class SaveRouteProcessViewModel : ViewModel() {
    val saveProgress: MutableLiveData<SaveProgress?> = MutableLiveData()

    @Suppress("unused")
    fun getSaveProgress(): SaveProgress? {
        return saveProgress.value
    }

    fun setSaveProgress(it: SaveProgress) {
        saveProgress.postValue(it)
    }
}

