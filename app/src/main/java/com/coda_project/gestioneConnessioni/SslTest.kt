package com.coda_project.gestioneConnessioni

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class SslTest : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}