package com.dacosys.assetControl.viewModel.route

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.assetControl.model.common.SaveProgress

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

