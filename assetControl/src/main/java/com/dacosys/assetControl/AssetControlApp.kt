package com.dacosys.assetControl

import android.app.Application
import android.content.Context
import com.dacosys.assetControl.utils.scanners.JotterListener
import id.pahlevikun.jotter.Jotter
import id.pahlevikun.jotter.event.ActivityEvent

class AssetControlApp : Application() {
    override fun onCreate() {
        super.onCreate()
        sApplication = this

        // Setup ImageControl context
        com.dacosys.imageControl.Statics.ImageControl().setAppContext(this)

        // Eventos del ciclo de vida de las actividades
        // que nos interesa interceptar para conectar y
        // desconectar los medios de lectura de códigos.
        Jotter.Builder(this)
            .setLogEnable(true)
            .setActivityEventFilter(
                listOf(
                    ActivityEvent.CREATE,
                    ActivityEvent.RESUME,
                    ActivityEvent.PAUSE,
                    ActivityEvent.DESTROY
                )
            )
            //.setFragmentEventFilter(listOf(FragmentEvent.VIEW_CREATE, FragmentEvent.PAUSE))
            .setJotterListener(JotterListener)
            .build()
            .startListening()
    }

    companion object {
        fun getContext(): Context {
            return getApplication()!!.applicationContext
        }

        private fun getApplication(): Application? {
            return sApplication
        }

        private var sApplication: Application? = null
    }
}