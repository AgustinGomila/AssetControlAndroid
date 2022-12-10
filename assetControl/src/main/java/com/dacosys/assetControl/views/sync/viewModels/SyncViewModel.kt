package com.dacosys.assetControl.views.sync.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.imageControl.main.UploadImagesProgress

class SyncViewModel : ViewModel() {
    val syncUploadProgress: MutableLiveData<SyncProgress?> = MutableLiveData()
    val uploadImagesProgress: MutableLiveData<UploadImagesProgress?> = MutableLiveData()
    val syncDownloadProgress: MutableLiveData<SyncProgress?> = MutableLiveData()
    val sessionCreated: MutableLiveData<Boolean?> = MutableLiveData()

    @Suppress("unused")
    fun getSyncUploadProgress(): SyncProgress? {
        return syncUploadProgress.value
    }

    fun setSyncUploadProgress(it: SyncProgress) {
        syncUploadProgress.postValue(it)
    }

    @Suppress("unused")
    fun getUploadImagesProgress(): UploadImagesProgress? {
        return uploadImagesProgress.value
    }

    fun setUploadImagesProgress(it: UploadImagesProgress) {
        uploadImagesProgress.postValue(it)
    }

    @Suppress("unused")
    fun getSyncDownloadProgress(): SyncProgress? {
        return syncDownloadProgress.value
    }

    fun setSyncDownloadProgress(it: SyncProgress) {
        syncDownloadProgress.postValue(it)
    }

    @Suppress("unused")
    fun getSessionCreated(): Boolean? {
        return sessionCreated.value
    }

    fun setSessionCreated(it: Boolean) {
        sessionCreated.postValue(it)
    }
}












