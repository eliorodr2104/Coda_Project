package com.coda_project.gestioneRisorse

import com.coda_project.gestioneConnessioni.HttpRequest
import java.util.LinkedList

class GestioneCode {
    var queueList = ArrayList<LinkedList<Int>>()

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

    fun resetSelectQueue(indexQueueClear: Int){
        queueList[indexQueueClear].clear()
    }

    fun resetAllQueue(){
        for (listaSingola in queueList){
            listaSingola.clear()
        }
    }
}