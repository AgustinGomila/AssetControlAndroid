package com.dacosys.assetControl.network.download

class DownloadTask(
    msg: String,
    fileType: FileType?,
    downloadStatus: DownloadStatus,
    progress: Int = 0,
    bytesCompleted: Long = 0,
    bytesTotal: Long = 0,
) {
    var msg: String = ""
    var fileType: FileType? = null
    var downloadStatus: DownloadStatus = DownloadStatus.INFO
    var progress: Int = 0
    var bytesCompleted: Long = 0
    var bytesTotal: Long = 0

    init {
        this.msg = msg
        this.fileType = fileType
        this.downloadStatus = downloadStatus
        this.progress = progress
        this.bytesCompleted = bytesCompleted
        this.bytesTotal = bytesTotal
    }
}