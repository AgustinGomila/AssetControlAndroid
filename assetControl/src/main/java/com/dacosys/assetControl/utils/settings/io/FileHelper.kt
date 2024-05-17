package com.dacosys.assetControl.utils.settings.io

import android.content.Context
import android.os.Environment
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.DATABASE_NAME
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.TEMP_DATABASE_NAME
import com.dacosys.assetControl.utils.Statics.Companion.IMAGE_CONTROL_DATABASE_NAME
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileHelper {
    companion object {
        private val tag = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName

        fun removeDataBases(context: Context) {
            Log.i(tag, "Eliminando bases de datos...")
            context.deleteDatabase(IMAGE_CONTROL_DATABASE_NAME)
            context.deleteDatabase(DATABASE_NAME)
            context.deleteDatabase(TEMP_DATABASE_NAME)
        }

        fun moveDatabaseFrom(context: Context, inputDbFile: File) {
            Log.d(tag, context.getString(R.string.copying_database))

            val outFile = context.getDatabasePath(DATABASE_NAME)

            Log.d(tag, "${context.getString(R.string.origin)}: ${inputDbFile.absolutePath}")
            Log.d(tag, "${context.getString(R.string.destination)}: $outFile")

            try {
                FileInputStream(inputDbFile).use { inputStream ->
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d(tag, context.getString(R.string.copy_ok))
            } catch (e: IOException) {
                Log.e(tag, "${context.getString(R.string.error_copying_database)}: ${e.message}")
            }
        }

        class CopyDbResult(var result: Boolean, var outFile: String)

        fun copyDbToDocuments(context: Context): CopyDbResult {
            try {
                val dbFile = File(context.getDatabasePath(DATABASE_NAME).toString())

                //Open your local db as the input stream
                val myInput = FileInputStream(dbFile)

                // Path to the just created empty db
                val outDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (outDir?.exists() != true) {
                    outDir?.mkdir()
                }

                val outFile = File(outDir, DATABASE_NAME)
                if (outFile.exists()) {
                    outFile.delete()
                }

                outFile.createNewFile()

                //Open the empty db as the output stream
                val myOutput = FileOutputStream(outFile)

                //transfer bytes from the input file to the outfile
                val buffer = ByteArray(1024)
                var length: Int
                while (run {
                        length = myInput.read(buffer)
                        length
                    } > 0) {
                    myOutput.write(buffer, 0, length)
                }

                //Close the streams
                myOutput.flush()
                myOutput.close()
                myInput.close()

                return CopyDbResult(true, outFile.path)
            } catch (ex: IOException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return CopyDbResult(false, "")
            }
        }
    }
}