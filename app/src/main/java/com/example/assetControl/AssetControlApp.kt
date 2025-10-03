package com.example.assetControl

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.dacosys.imageControl.ImageControl
import com.example.assetControl.data.room.dto.user.User
import com.example.assetControl.data.room.repository.user.UserRepository
import com.example.assetControl.devices.deviceLifecycle.DeviceLifecycle
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.settings.preferences.SettingsRepository
import com.example.assetControl.utils.settings.preferences.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin

class AssetControlApp : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AssetControlApp)
            modules(appModule)
        }
        initDeviceListening()
    }

    private fun initDeviceListening() {
        /** Star listening activities events **/
        deviceLifecycle.startListening()
    }

    companion object {
        val context: Context by lazy { get().get() }
        val deviceLifecycle: DeviceLifecycle by lazy { get().get() }
        val appName: String by lazy { "${getApplicationName()}${MILESTONE}" }
        val imageControl: ImageControl? by lazy { get().get() }
        val sr: SettingsRepository by lazy { get().get() }
        val prefs: SharedPreferences by lazy { get().get() }
        val svm: SettingsViewModel by lazy { get().get() }

        private fun getApplicationName(): String {
            val applicationInfo = context.applicationInfo
            return when (val stringId = applicationInfo.labelRes) {
                0 -> applicationInfo.nonLocalizedLabel.toString()
                else -> context.getString(stringId)
            }
        }

        private const val MILESTONE = "M13"
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
            return if (Statics.GOD_MODE) true else
                (currentUserId ?: 0L) > 0L
        }

        fun getUserId(): Long? = currentUserId

        fun setCurrentUserId(userId: Long? = null) {
            currentUserId = userId
        }
    }
}