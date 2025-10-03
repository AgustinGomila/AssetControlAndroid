package com.example.assetControl.viewModel.sync

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import com.example.assetControl.network.sync.SyncProgress

class SyncViewModel : ViewModel() {
    val syncUploadProgress: MutableLiveData<SyncProgress?> = MutableLiveData()
    val uploadImagesProgress: MutableLiveData<UploadImagesProgress?> = MutableLiveData()
    val syncDownloadProgress: MutableLiveData<SyncProgress?> = MutableLiveData()
    val syncTimerProgress: MutableLiveData<Int?> = MutableLiveData()
    val sessionCreated: MutableLiveData<Boolean?> = MutableLiveData()

    fun getSyncUploadProgress(): SyncProgress? {
        return syncUploadProgress.value
    }

    fun setSyncUploadProgress(it: SyncProgress) {
        syncUploadProgress.postValue(it)
    }

    fun getUploadImagesProgress(): UploadImagesProgress? {
        return uploadImagesProgress.value
    }

    fun setUploadImagesProgress(it: UploadImagesProgress) {
        uploadImagesProgress.postValue(it)
    }

    fun getSyncDownloadProgress(): SyncProgress? {
        return syncDownloadProgress.value
    }

    fun setSyncDownloadProgress(it: SyncProgress) {
        syncDownloadProgress.postValue(it)
    }

    fun getSessionCreated(): Boolean? {
        return sessionCreated.value
    }

    fun setSessionCreated(it: Boolean) {
        sessionCreated.postValue(it)
    }

    fun getSyncTimerProgress(): Int {
        return syncTimerProgress.value ?: 0
    }

    fun setSyncTimerProgress(it: Int) {
        syncTimerProgress.postValue(it)
    }
}