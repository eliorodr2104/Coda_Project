package com.coda_project.gestioneRisorse

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class GestioneRisorse(private val context: Context) {

    fun readDataFile(
        nameFile: String
    ): String{
        val fileInputStream: FileInputStream //Variabile per leggere il file della memoria di massa

        val filePath: String = (context.filesDir.absolutePath + File.separator) + nameFile

        val splitFile = nameFile.split("/")[1]

        return if (File(filePath).exists()){
            try {
                fileInputStream = context.openFileInput(splitFile)

                val inputStream = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStream)

                val stringBuilder = StringBuilder("")
                var text: String?

                while (run {
                        text = bufferedReader.readLine()
                        text

                    } != null){
                    stringBuilder.append(text)
                }

                stringBuilder.toString()
            }catch (e: Exception){
                "ERROR"
            }

        }else{
            writeDataFile("", nameFile)
            ""
        }
    }

    fun writeDataFile(
        textToWrite: String,
        nameFile: String
    ): Boolean{
        val fileOutputStream: FileOutputStream //Variabile per scrivere il file nella memoria di massa

        return try {
            val dirPath: String = (context.filesDir.absolutePath + File.separator) + DataFiles.PRINCIPAL_FOLDER_NAME.valore
            val filePath: String = (context.filesDir.absolutePath + File.separator) + nameFile

            val splitFile = nameFile.split("/")[1]

            val projDir = File(dirPath)
            val projFile = File(filePath)

            if (!projDir.exists())
                projDir.mkdirs()

            if (!projFile.exists())
                projFile.createNewFile()

            fileOutputStream = context.openFileOutput(splitFile, Context.MODE_PRIVATE)
            fileOutputStream.write(textToWrite.toByteArray())

            true
        }catch (e: Exception){
            false
        }
    }
}