package com.dacosys.assetControl

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.dacosys.assetControl.data.room.entity.user.User
import com.dacosys.assetControl.data.room.repository.user.UserRepository
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

        private var sApplication: Application? = null
        private fun getApplication(): Application? {
            return sApplication
        }

        val appName: String
            get() = "${getApplicationName()}M13"

        private fun getApplicationName(): String {
            val applicationInfo = getContext().applicationInfo
            return when (val stringId = applicationInfo.labelRes) {
                0 -> applicationInfo.nonLocalizedLabel.toString()
                else -> getContext().getString(stringId)
            }
        }

        private var currentUserId: Long? = null

        /**
         * Current user
         * Se utiliza generalmente para obtener el nombre del usuario actual
         * necesario en algunos procesos.
         * @return
         */
        fun currentUser(): User? {
            val userId = currentUserId
            return if (userId != null) UserRepository().selectById(userId)
            else null
        }

        fun isLogged(): Boolean {
            return (currentUserId ?: 0L) > 0L
        }

        fun getUserId(): Long? {
            return currentUserId
        }

        fun setCurrentUserId(userId: Long? = null) {
            currentUserId = userId
        }
    }
}