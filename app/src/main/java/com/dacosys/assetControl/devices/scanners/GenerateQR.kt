package com.dacosys.assetControl.devices.scanners

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class GenerateQR(data: String, size: android.util.Size, onProgress: (Int) -> Unit, onFinish: (Bitmap?) -> Unit) {
    init {
        val writer = QRCodeWriter()
        try {
            var w: Int = size.width
            val h: Int = size.height
            if (h < w) w = h

            // CREAR LA IMAGEN
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, w, w)
            val width = bitMatrix.width
            val height = bitMatrix.height

            val total = width * height
            var current = 0
            var lastValue = 0

            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width

                for (x in 0 until width) {
                    current++
                    val value: Int = current * 100 / total

                    if (value != lastValue) {
                        lastValue = value

                        Log.d(javaClass.simpleName, "Generando QR: ${value}% (${current}/${total})")
                        onProgress.invoke(value)
                    }

                    val color: Int = if (bitMatrix.get(x, y)) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }

                    pixels[offset + x] = color
                }
            }

            val bmp = createBitmap(width, height)
            bmp.setPixels(pixels, 0, width, 0, 0, width, height)

            onFinish.invoke(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
            onFinish.invoke(null)
        }
    }
}
