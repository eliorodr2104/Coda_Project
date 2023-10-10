package com.coda_project

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get

class HttpRequest(
    var urlServer: String
) {

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 4000
        }
    }

    /**
     * Metodo getAggiornamentoCoda()
     * @param indiceCatalogo: String = Prende l'indice del catalogo da prendere
     * @return Un array di json con dieci libri appartenenti al catalogo
     */
    suspend fun getAggiornamentoCoda(numeroCoda: String): String? {


        return try {

            //&coda=${numeroCoda}
            val risposta = client.get("$urlServer&coda=${numeroCoda}")

            //Controllo per la risposta giusta del server
            if (risposta.status.value in 200..299)
                "200"
            else
                null

        } catch (s: Exception) {
            "Server timeout connection"
        }



        //return "200"
    }
}