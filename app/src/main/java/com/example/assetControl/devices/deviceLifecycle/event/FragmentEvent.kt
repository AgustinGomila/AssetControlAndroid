package com.example.assetControl.devices.deviceLifecycle.event

import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    FragmentEvent.PRE_ATTACH,
    FragmentEvent.ATTACH,
    FragmentEvent.CREATE,
    FragmentEvent.ACTIVITY_CREATE,
    FragmentEvent.PRE_CREATE,
    FragmentEvent.VIEW_CREATE,
    FragmentEvent.START,
    FragmentEvent.RESUME,
    FragmentEvent.PAUSE,
    FragmentEvent.STOP,
    FragmentEvent.SAVE_INSTANCE_STATE,
    FragmentEvent.DESTROY,
    FragmentEvent.VIEW_DESTROY,
    FragmentEvent.DETACH
)
annotation class FragmentEvent {
    companion object {
        const val PRE_ATTACH = "PRE_ATTACH"
        const val ATTACH = "ATTACH"
        const val ACTIVITY_CREATE = "ACTIVITY_CREATE"
        const val CREATE = "CREATE"
        const val PRE_CREATE = "PRE_CREATE"
        const val VIEW_CREATE = "VIEW_CREATE"
        const val START = "START"
        const val RESUME = "RESUME"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
        const val SAVE_INSTANCE_STATE = "SAVE_INSTANCE_STATE"
        const val DESTROY = "DESTROY"
        const val VIEW_DESTROY = "VIEW_DESTROY"
        const val DETACH = "DETACH"
    }
}