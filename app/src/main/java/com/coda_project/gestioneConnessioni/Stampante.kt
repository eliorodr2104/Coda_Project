package com.coda_project.gestioneConnessioni

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID


class Stampante(
    var indirizzoMacStampante: String,
    private val context: Context
) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private var mmInputStream: InputStream? = null
    private var mmOutputStream: OutputStream? = null
    private var workerThread: Thread? = null

    private lateinit var readBuffer: ByteArray
    private var readBufferPosition = 0

    @Volatile
    private var stopWorker = false

    fun findBT() {
        try {

            val pairedDevices: Set<BluetoothDevice> = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                emptySet()
            } else {
                bluetoothAdapter?.bondedDevices ?: emptySet()
            }

            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {


                    if (device.name == indirizzoMacStampante) {
                        bluetoothDevice = device
                        break
                    }
                }
            }
            //myLabel.setText("Bluetooth Device Found")
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun connectToPrinter() {
        try {
            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                mmOutputStream = bluetoothSocket?.outputStream
                mmInputStream = bluetoothSocket?.inputStream
                beginListenForData()
            }


            //myLabel.setText("Bluetooth Opened")
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun beginListenForData() {
        if (mmInputStream != null) {
            try {
                val handler = Handler()

                val delimiter: Byte = 10
                stopWorker = false
                readBufferPosition = 0
                readBuffer = ByteArray(1024)
                workerThread = Thread {
                    while (!Thread.currentThread().isInterrupted && !stopWorker){
                        try {
                            val bytesAvailable = mmInputStream!!.available()
                            if (bytesAvailable > 0){
                                val packetBytes = ByteArray(bytesAvailable)
                                mmInputStream!!.read(packetBytes)
                                for (i in 0 until bytesAvailable){
                                    val b = packetBytes[i]
                                    if (b == delimiter){
                                        val encodedByte = ByteArray(readBufferPosition)
                                        System.arraycopy(
                                            readBuffer, 0,
                                            encodedByte, 0,
                                            encodedByte.size
                                        )

                                        val data = String(encodedByte, Charset.forName("US-ASCII"))
                                        readBufferPosition = 0

                                        handler.post { Log.d("e", data)}

                                    }else {
                                        readBuffer[readBufferPosition++] = b
                                    }
                                }
                            }
                        }catch (ex: IOException){
                            stopWorker = true
                        }
                    }
                }
                workerThread!!.start()
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun sendPrintCommand(text: String, queueName: String): Boolean {
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        val formattedDate = currentDate.format(dateFormatter)
        val formattedTime = currentTime.format(timeFormatter)

        var esito = false

        val commandBytes = byteArrayOf(0x1B, 0x40)

        val styleHeader = byteArrayOf(0x1B, 0x61, 0x01)

        val headerWelcome = "Benvenuto!\n\n"

        val headerTicket = "N.sportello: $queueName\n-------------------------------\n"

        val styleLeft = byteArrayOf(0x1B, 0x61, 0x00)
        val styleRight = byteArrayOf(0x1B, 0x61, 0x02)

        val ticket = "N.ticket: $text\n-------------------------------\n"

        val footTicket = "Grazie e arrivederci!\n\n"

        val footTimeAndDate = "$formattedDate             $formattedTime"

        val charBytesHeader = headerTicket.toByteArray(Charset.forName("UTF-8"))
        val charBytesWelcome = headerWelcome.toByteArray(Charset.forName("UTF-8"))

        val charBytesNumberTicker = ticket.toByteArray(Charset.forName("UTF-8"))
        val charBytesFoot = footTicket.toByteArray(Charset.forName("UTF-8"))

        val charBytesFootDateAndTime = footTimeAndDate.toByteArray(Charset.forName("UTF-8"))

        val avanzamentoCarta = byteArrayOf(0x0A, 0x0A)

        val cutPartialPaper = byteArrayOf(0x1D, 0x56, 0x01)

        if (bluetoothSocket?.isConnected == true) {
            try {
                try {

                    mmOutputStream?.write(commandBytes)

                    mmOutputStream?.write(styleHeader)

                    mmOutputStream?.write(charBytesWelcome)

                    mmOutputStream?.write(styleLeft)

                    mmOutputStream?.write(charBytesHeader)

                    mmOutputStream?.write(charBytesNumberTicker)

                    mmOutputStream?.write(styleHeader)

                    mmOutputStream?.write(charBytesFoot)

                    mmOutputStream?.write(styleLeft)

                    mmOutputStream?.write(charBytesFootDateAndTime)

                    mmOutputStream?.write(avanzamentoCarta)
                    mmOutputStream?.write(avanzamentoCarta)

                    mmOutputStream?.write(cutPartialPaper)

                    mmOutputStream?.flush()

                    esito = true

                } catch (ex: java.lang.Exception) {
                    Toast.makeText(context, ex.message.toString(), Toast.LENGTH_LONG).show()
                }
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }

        return esito
    }

    fun closeConnection() {
        bluetoothSocket?.close()
    }

    fun getConnectionPrinter(): Boolean{
        return bluetoothSocket?.isConnected ?: false
    }
}