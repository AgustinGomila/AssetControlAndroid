package com.dacosys.assetControl.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import com.dacosys.assetControl.AssetControlApp
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

class FileHelper {
    companion object {

        private var contentUri: Uri? = null

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.<br>
         * <br>
         * Callers should check whether the path is local before assuming it
         * represents a local file.
         *
         * @param uri     The Uri to query.
         */

        @Suppress("UNUSED_VARIABLE")
        @SuppressLint("NewApi")
        fun getPath(uri: Uri): String? {
            val selection: String?
            val selectionArgs: Array<String>?

            // DocumentProvider
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                val fullPath = getPathFromExtSD(split)
                return if (fullPath !== "") {
                    fullPath
                } else {
                    null
                }
            }

            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                var cursor: Cursor? = null
                try {
                    cursor = AssetControlApp.getContext().contentResolver.query(
                        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName: String = cursor.getString(0)
                        val path: String = AssetControlApp.getContext()
                            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                            .toString() + "/Download/" + fileName
                        if (!TextUtils.isEmpty(path)) {
                            return path
                        }
                    }
                } finally {
                    cursor?.close()
                }
                val id: String = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads", "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        return try {
                            val contentUri: Uri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id)
                            )
                            getDataColumn(contentUri, "", null)
                        } catch (e: NumberFormatException) {
                            //In Android 8 and Android P the id is not a number
                            (uri.path ?: return null).replaceFirst("^/document/raw:", "")
                                .replaceFirst("^raw:", "")
                        }
                    }
                }
            }

            // MediaProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }

                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }

                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return getDataColumn(contentUri ?: return null, selection, selectionArgs)
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri)
            }
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri)
                }
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // return getFilePathFromURI(context,uri);
                    copyFileToInternalStorage(uri, "userfiles")
                    // return getRealPathFromURI(context,uri);
                } else {
                    getDataColumn(uri, "", null)
                }
            }
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/" + pathData[1]
            var fullPath: String

            // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage
            // don't know any API that can get the root path of that storage based on its id.
            //
            // so no "primary" type, but let the check here for other devices
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = AssetControlApp.getContext()
                    .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            // Environment.isExternalStorageRemovable() is `true` for external and internal storage
            // so we cannot relay on it.
            //
            // instead, for each possible path, check if file exists
            // we'll start with secondary storage as this could be our (physically) removable sd card
            fullPath = System.getenv("SECONDARY_STORAGE") ?: ("" + relativePath)
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE") ?: ("" + relativePath)
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        @Suppress("UNUSED_VARIABLE")
        private fun getDriveFilePath(uri: Uri): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = AssetControlApp.getContext().contentResolver.query(
                returnUri, null, null, null, null
            ) ?: return null
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()
            val file = File(AssetControlApp.getContext().cacheDir, name)

            try {
                val inputStream: InputStream =
                    AssetControlApp.getContext().contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable: Int = inputStream.available()

                //int bufferSize = 1024;
                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Log.e("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
            } catch (e: java.lang.Exception) {
                Log.e("Exception", e.message ?: "")
            }
            returnCursor.close()
            return file.path
        }

        /***
         * Used for Android Q+
         * @param uri
         * @param newDirName if you want to create a directory, you can set this variable
         * @return
         */
        @Suppress("UNUSED_VARIABLE")
        private fun copyFileToInternalStorage(
            uri: Uri,
            newDirName: String,
        ): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = AssetControlApp.getContext().contentResolver.query(
                returnUri, arrayOf(
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                ), null, null, null
            ) ?: return null

            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()

            val output = if (newDirName != "") {
                val dir = File(AssetControlApp.getContext().filesDir.toString() + "/" + newDirName)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                File(AssetControlApp.getContext().filesDir.toString() + "/" + newDirName + "/" + name)
            } else {
                File(AssetControlApp.getContext().filesDir.toString() + "/" + name)
            }

            try {
                val inputStream: InputStream =
                    AssetControlApp.getContext().contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(output)
                var read: Int
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
            } catch (e: java.lang.Exception) {
                Log.e("Exception", e.message ?: "")
            }
            returnCursor.close()
            return output.path
        }

        private fun getFilePathForWhatsApp(uri: Uri): String? {
            return copyFileToInternalStorage(uri, "whatsapp")
        }

        private fun getDataColumn(
            uri: Uri,
            selection: String,
            selectionArgs: Array<String>?,
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = AssetControlApp.getContext().contentResolver.query(
                    uri, projection, selection, selectionArgs, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        private fun isWhatsAppFile(uri: Uri): Boolean {
            return "com.whatsapp.provider.media" == uri.authority
        }

        private fun isGoogleDriveUri(uri: Uri): Boolean {
            return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
        }
    }
}