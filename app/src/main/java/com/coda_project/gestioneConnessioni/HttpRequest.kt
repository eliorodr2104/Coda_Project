package com.coda_project.gestioneConnessioni

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.InternalAPI
import io.ktor.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.SecureRandom
import java.util.LinkedList
import javax.net.ssl.SSLContext

class HttpRequest(
    var urlServer: String
) {

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                val trustAllCerts = SslTest()
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                sslSocketFactory(sslSocketFactory, trustAllCerts)

                hostnameVerifier { _, _ -> true }
            }
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun postAggiornamentoCoda(numeroCoda: String, coda: LinkedList<Int>): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = withContext(Dispatchers.IO) {
            ObjectOutputStream(byteArrayOutputStream)
        }
        withContext(Dispatchers.IO) {
            objectOutputStream.writeObject(coda)
        }
        withContext(Dispatchers.IO) {
            objectOutputStream.flush()
        }
        val byteArray = byteArrayOutputStream.toByteArray()

        val response = client.post("http://192.168.1.121:8080/code/upload/$numeroCoda") {
            contentType(ContentType.Application.OctetStream)
            body = ByteArrayContent(byteArray, contentType = ContentType.Application.OctetStream)

            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }

        return if (response.status.value in 200..299)
            "200"
        else
            null
    }

    @OptIn(InternalAPI::class)
    suspend fun downloadLinkedList(numCoda: String): LinkedList<Int> {
        val response = client.get("http://192.168.1.121:8080/code/download/$numCoda")

        return if (response.status.isSuccess()) {
            try {
                val byteArray = response.content.toByteArray()

                val objectInputStream =
                    withContext(Dispatchers.IO) {
                        ObjectInputStream(ByteArrayInputStream(byteArray))
                    }

                val linkedList: LinkedList<Int> =
                    withContext(Dispatchers.IO) {
                        objectInputStream.readObject()
                    } as LinkedList<Int>

                withContext(Dispatchers.IO) {
                    objectInputStream.close()
                }
                linkedList
            } catch (e: Exception) {
                // Gestisci eventuali errori nella lettura o nella conversione dei dati
                LinkedList<Int>()
            }
        } else {
            // Gestisci il caso in cui la richiesta non sia andata a buon fine
            LinkedList<Int>()
        }
    }

}