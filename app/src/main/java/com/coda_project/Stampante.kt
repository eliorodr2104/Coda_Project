package com.coda_project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


class Stampante(
    var indirizzoMacStampante: String,
    private val context: Context
) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

    init {
        connectToPrinter()
    }

    fun connectToPrinter() {
        try {
            val printerDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(indirizzoMacStampante)

            printerDevice?.let {
                val uuid: UUID? = if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                ) {
                    it.uuids[0].uuid

                } else {
                    null
                }

                try {
                    bluetoothSocket = it.createRfcommSocketToServiceRecord(uuid)

                    // Richiedi l'autorizzazione all'utente
                    if (bluetoothAdapter?.isEnabled == true) {
                        bluetoothSocket?.connect()

                    }

                } catch (e: IOException) {
                    // Gestisci eventuali eccezioni durante la connessione
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            // Gestisci l'eccezione
            e.printStackTrace()
            println(e.message)
        }
    }


    fun sendPrintCommand(text: String): Boolean {
        bluetoothSocket?.let { socket ->
            try {
                val outputStream: OutputStream = socket.outputStream
                val commandBytes = text.toByteArray(Charsets.UTF_8)

                outputStream.write(commandBytes)
                outputStream.flush()

                return true
            } catch (e: Exception) {
                // Gestisci eventuali eccezioni durante la stampa
                e.printStackTrace()

                return  false
            }
        }

        return false
    }

    fun closeConnection() {
        bluetoothSocket?.close()
    }

    fun getConnectionPrinter(): Boolean{
        return bluetoothSocket?.isConnected ?: false
    }
}