package com.dacosys.assetControl.network.trust

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.network.trust.TrustFactory.Companion.addTrustedDomains
import com.dacosys.assetControl.network.trust.TrustFactory.Companion.trustedDomains
import com.dacosys.assetControl.utils.settings.preferences.Repository
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

object CustomSSLContext {
    fun createCustomSSLContext(): SSLContext? {
        try {
            val url = Repository.wsUrl
            if (url.isNotEmpty()) addTrustedDomains(listOf(URL(url).host))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        return try {
            val (socketFactory, defaultTrustManager) = TrustFactory.getTrustFactoryManager(getContext())
            val customTrustManager = CustomTrustManager(defaultTrustManager)

            for (domain in trustedDomains) {
                customTrustManager.addTrustedDomain(domain)
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(customTrustManager), null)
            sslContext
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            null
        }
    }
}