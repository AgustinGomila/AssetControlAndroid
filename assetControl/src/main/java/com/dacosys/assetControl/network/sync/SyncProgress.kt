package com.dacosys.assetControl.network.sync

import com.dacosys.assetControl.network.utils.ProgressStatus

class SyncProgress(
    var totalTask: Int = 0,
    var completedTask: Int = 0,
    var msg: String = "",
    var registryType: SyncRegistryType? = null,
    var uniqueId: String = "",
    var progressStatus: ProgressStatus = ProgressStatus.unknown,
)
