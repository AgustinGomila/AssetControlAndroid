package com.dacosys.assetControl.views.sync.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.assetControl.network.download.DownloadTask
import com.dacosys.assetControl.views.commons.snackbar.SnackBarEventData

class DownloadDbViewModel : ViewModel() {
    val downloadTaskEvent: MutableLiveData<DownloadTask?> = MutableLiveData()
    val uiEvent: MutableLiveData<SnackBarEventData?> = MutableLiveData()

    @Suppress("unused")
    fun getDownloadTask(): DownloadTask? {
        return downloadTaskEvent.value
    }

    fun setDownloadTask(it: DownloadTask) {
        downloadTaskEvent.postValue(it)
    }

    @Suppress("unused")
    fun getUiEvent(): SnackBarEventData? {
        return uiEvent.value
    }

    fun setUiEvent(it: SnackBarEventData) {
        uiEvent.postValue(it)
    }
}