package com.dacosys.assetControl.network.serverDate

import com.dacosys.assetControl.network.utils.ProgressStatus

/**
 * status: Indica cómo terminó el proceso de obtención de la fecha en el servidor
 * msg: Si status es finished entonces msg es la fecha devuelta por el servidor.
 * Sino es la descripción del error.
 */

class MySqlDateResult(status: ProgressStatus, msg: String) {
    var status: ProgressStatus = ProgressStatus.unknown
    var msg: String = ""

    init {
        this.status = status
        this.msg = msg
    }
}