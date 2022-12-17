package com.dacosys.assetControl.views.routes.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.assetControl.model.commons.SaveProgress

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

