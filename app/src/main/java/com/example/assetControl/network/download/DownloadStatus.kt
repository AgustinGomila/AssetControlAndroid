package com.example.assetControl.network.download

/**
 * Tiene los diferentes estados durante una descarga
 */
enum class DownloadStatus(val id: Long) {
    STARTING(1), DOWNLOADING(2), CANCELED(3), FINISHED(4), CRASHED(5), INFO(6)
}