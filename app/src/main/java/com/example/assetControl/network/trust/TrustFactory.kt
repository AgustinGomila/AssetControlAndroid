package com.example.assetControl.network.trust

import android.content.Context
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class TrustFactory {
    companion object {

        private const val DACOSYS_CONFIG = "config.dacosys.com"
        private const val DACOSYS_CLIENT = "client.example.com"

        private var trustedList: MutableList<String> = mutableListOf(
            DACOSYS_CONFIG,
            DACOSYS_CLIENT,
        )

        val trustedDomains: List<String>
            get() = trustedList

        fun addTrustedDomains(domains: List<String>) {
            domains
                .filterNot { trustedList.contains(it) }
                .forEach { trustedList.add(it) }
        }

        fun getTrustFactoryManager(
            context: Context,
            certResourceIds: List<Int>
        ): Pair<SSLSocketFactory, X509TrustManager> {
            val cf = CertificateFactory.getInstance("X.509")
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }

            // Cargar los certificados desde los recursos `raw`
            certResourceIds.forEachIndexed { index, resId ->
                context.resources.openRawResource(resId).use { inputStream ->
                    val certificate: Certificate = cf.generateCertificate(inputStream)
                    keyStore.setCertificateEntry("cert_$index", certificate)
                }
            }

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, tmf.trustManagers, null)
            }

            return Pair(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
        }
    }
}