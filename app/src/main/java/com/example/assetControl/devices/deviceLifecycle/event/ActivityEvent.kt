package com.example.assetControl.devices.deviceLifecycle.event

import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    ActivityEvent.CREATE,
    ActivityEvent.START,
    ActivityEvent.RESUME,
    ActivityEvent.PAUSE,
    ActivityEvent.STOP,
    ActivityEvent.SAVE_INSTANCE_STATE,
    ActivityEvent.DESTROY
)
annotation class ActivityEvent {
    companion object {
        const val CREATE = "CREATE"
        const val START = "START"
        const val RESUME = "RESUME"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
        const val SAVE_INSTANCE_STATE = "SAVE_INSTANCE_STATE"
        const val DESTROY = "DESTROY"

        val scannerListenerEvents = listOf(
            CREATE,
            RESUME,
            PAUSE,
            DESTROY
        )
    }
}