package com.coda_project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.LinkedList

class GestioneCode {
    /*
    private val codaUno = LinkedList<Int>().apply {
        add(0)
        add(1)
    }

    private val codaDue = LinkedList<Int>().apply {
        add(0)
    }

    private val codaTre = LinkedList<Int>().apply {
        add(0)
    }

     */

    var queueList by mutableStateOf(LinkedList<LinkedList<Int>>())

    fun aumentaCoda(indexQueue: Int){
        queueList[indexQueue].add(queueList[indexQueue].size + 1)
    }

    fun togliPersoneCoda(indexQueue: Int){
        if (queueList[indexQueue].size - 1 >= 0)
            queueList[indexQueue].pollLast()

        else
            queueList[indexQueue].clear()
    }

    fun aggiungiCode(numeroCodeDaAggiungere: Int){
        for (i in 0 until numeroCodeDaAggiungere){
            queueList.add(LinkedList<Int>())
        }
    }
}