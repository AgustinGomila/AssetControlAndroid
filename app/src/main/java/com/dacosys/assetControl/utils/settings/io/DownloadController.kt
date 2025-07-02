package com.dacosys.assetControl.utils.settings.io

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import java.io.File

class DownloadController(private val view: View) {

    companion object {
        private const val FILE_NAME = "assetControl-release.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"

        private const val apkUrl =
            "http://resources.dacosys.com/Asset_Control/Milestone13/installers/android/assetControl-release.apk"
    }

    fun enqueueDownload() {
        val context = context

        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        val uri = "$FILE_BASE_PATH$destination".toUri()

        val file = File(destination)
        if (file.exists()) file.delete()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = apkUrl.toUri()
        val request = DownloadManager.Request(downloadUri)

        request.setMimeType(MIME_TYPE)
        request.setTitle(context.getString(R.string.apk_is_downloading))
        request.setDescription(context.getString(R.string.downloading_))

        // set destination
        request.setDestinationUri(uri)

        showInstallOption(context, destination)

        // Enqueue a new download and same the referenceId
        downloadManager.enqueue(request)
        makeText(view, context.getString(R.string.downloading_), INFO)
    }

    private fun showInstallOption(
        context: Context,
        destination: String,
    ) {
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                installAPK(context, destination)
                context.unregisterReceiver(this)
            }
        }

        ContextCompat.registerReceiver(
            context,
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun installAPK(context: Context, destination: String) {
        val file = File(destination)
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uriFromFile(context, destination), MIME_TYPE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                makeText(
                    view,
                    context.getString(R.string.error_opening_the_file),
                    ERROR
                )
            }
        } else {
            makeText(view, context.getString(R.string.file_not_found), ERROR)
        }
    }

    private fun uriFromFile(context: Context, destination: String): Uri? {
        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + PROVIDER_PATH,
            File(destination)
        )
    }
}