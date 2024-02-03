package com.dacosys.assetControl

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.dacosys.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.dacosys.assetControl.utils.imageControl.ImageControl.Companion.imageControl
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefs
import com.dacosys.imageControl.ImageControl
import id.pahlevikun.jotter.Jotter
import id.pahlevikun.jotter.event.ActivityEvent

class AssetControlApp : Application() {
    override fun onCreate() {
        super.onCreate()
        sApplication = this

        // Setup ImageControl context
        imageControl = ImageControl.Builder(INTERNAL_IMAGE_CONTROL_APP_ID).build()

        // Shared Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext())

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
            .setJotterListener(JotterListener).build().startListening()
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