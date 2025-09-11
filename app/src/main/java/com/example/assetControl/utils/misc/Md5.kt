package com.example.assetControl.utils.misc

import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Md5 {
    companion object {

        /**
         * Md5 hash to int
         *
         * @param hash
         * @return Entero de 10 dígitos de longitud que puede usarse como identificador
         */
        fun md5HashToInt(hash: String): Long {
            val bigInt = BigInteger(hash, 16)
            val maxVal = BigInteger.TEN.pow(10).subtract(BigInteger.ONE)
            return bigInt.mod(maxVal).toLong()
        }

        fun getMd5(text: String): String {
            return String(encodeHex(md5Bytes(getRawBytes(text))))
        }

        private val DIGITS_LOWER = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f'
        )
        private val DIGITS_UPPER = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )

        private fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): CharArray {
            return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
        }

        private fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
            val l = data.size
            val out = CharArray(l shl 1)
            var i = 0
            var j = 0
            while (i < l) {
                out[j++] = toDigits[(240 and data[i].toInt()).ushr(4)]
                out[j++] = toDigits[15 and data[i].toInt()]
                i++
            }
            return out
        }

        private fun getRawBytes(text: String): ByteArray {
            return try {
                text.toByteArray(Charsets.UTF_8)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                text.toByteArray()
            }
        }

        private fun md5Bytes(data: ByteArray): ByteArray {
            return getDigest().digest(data)
        }

        private fun getDigest(algorithm: String = "MD5"): MessageDigest {
            try {
                return MessageDigest.getInstance(algorithm)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                throw IllegalArgumentException(e)
            }
        }
    }
}