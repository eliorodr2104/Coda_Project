package com.coda_project.interfacceGrafiche

import android.R
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coda_project.gestioneRisorse.DataFiles
import com.coda_project.gestioneRisorse.GestioneCode
import com.coda_project.gestioneRisorse.GestioneRisorse
import com.coda_project.gestioneConnessioni.HttpRequest
import com.coda_project.gestioneConnessioni.Stampante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class GuestUI(context: Context) {
    private var gestioneRisorse = GestioneRisorse(context = context)

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MenuIniziale(
        contentPadding: PaddingValues,
        stampante: Stampante,
        httpRequest: HttpRequest,
        code: MutableState<GestioneCode>
    ){

        LazyButtons(
            stampante = stampante,
            contentPadding = contentPadding,
            httpRequest = httpRequest,
            code = code
        )
    }

    @Composable
    private fun AlertMessageBox(
        title: String,
        message: String,
        onDismiss: () -> Unit,
    ) {
        val showDialog = remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun LazyButtons(
        stampante: Stampante,
        contentPadding: PaddingValues,
        httpRequest: HttpRequest,
        code: MutableState<GestioneCode>
    ) {
        val showAlert = remember { mutableStateOf(false) }
        val showAlertError = remember { mutableStateOf(false) }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date.from(Instant.now())
        val formattedDate = dateFormat.format(currentDate)

        val coroutineScope = rememberCoroutineScope()

        // Conto alla rovescia per cambiare automaticamente il pulsante dopo 3 secondi
        LaunchedEffect(showAlert.value) {
            if (showAlert.value) {
                delay(5000) // Attendiamo 3 secondi

                showAlert.value = false
            }
        }

        if (!showAlert.value){
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(200.dp),
                contentPadding = PaddingValues(all = 15.dp),
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                items(code.value.queueList.size) { index ->
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(4.dp, Color.Black),
                        modifier = Modifier
                            .clickable(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    addPersonToQueue(
                                        httpRequest = httpRequest,
                                        index = index,
                                        code = code,
                                        stampante = stampante,
                                        showAlert = showAlert,
                                        showAlertError = showAlertError
                                    )
                                }

                                //httpRequest.updateQueueStatus(index)
                            }),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Image(
                                    painter = painterResource(com.coda_project.R.drawable.icons8_queue_48___), // Replace R.drawable.my_image with your drawable resource ID
                                    contentDescription = "My Image",
                                    modifier = Modifier
                                        .size(55.dp) // Set the size of the image
                                        .padding(
                                            top = 15.dp,
                                            start = 25.dp
                                        )
                                )

                                Text(
                                    text = "Nro: $index",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(
                                        top = 20.dp,
                                        start = 5.dp
                                    ),
                                    softWrap = false
                                )
                            }

                            Text(
                                text = "Numero del biglietto:",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 25.dp, top = 10.dp)
                            )

                            Text(
                                text = (code.value.queueList[index].size).toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 25.dp, bottom = 15.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .padding(
                                            end = 15.dp,
                                            bottom = 10.dp
                                        )
                                        .weight(1f),
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

        }else {
            AlertMessageBox(
                title = if (!showAlertError.value) "Operazione conclusa!" else "Errore!",
                message = if (!showAlertError.value) "Per favore, ritiri il biglietto" else "Siamo spiacenti, ma si è verificato un errore nell'operazione.\n" +
                        "Si prega di contattare l'assistenza immediatamente. \n" +
                        "Ci scusiamo per il disagio.",
                onDismiss = {
                    showAlert.value = false
                    showAlertError.value = false
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun addPersonToQueue(
        httpRequest: HttpRequest,
        index: Int,
        code: MutableState<GestioneCode>,
        stampante: Stampante,
        showAlert: MutableState<Boolean>,
        showAlertError: MutableState<Boolean>
    ) {
        val aggiornamentoCodaResult = httpRequest.postAggiornamentoCoda(index.toString(), code.value.queueList[index])

        if (aggiornamentoCodaResult == "200") {
            code.value.aumentaCoda(index)

            val queueSize = code.value.queueList[index].size.toString()
            //val sendPrintResult = stampante.sendPrintCommand(queueSize, index.toString())

            showAlert.value = true
            /*
            if (sendPrintResult) {
                showAlert.value = true
            } else {
                showAlertError.value = true
                showAlert.value = true
                code.value.togliPersoneCoda(index)
            }

             */

        } else {
            showAlertError.value = true
            showAlert.value = true
        }
    }


    /*
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addPersonToQueue(
        httpRequest: HttpRequest,
        index: Int,
        code: MutableState<GestioneCode>,
        stampante: Stampante,
        showAlert: MutableState<Boolean>,
        showAlertError: MutableState<Boolean>
    ){
        if (httpRequest.getAggiornamentoCoda(index.toString()) == "200") {

            code.value.aumentaCoda(index)

            if (stampante.sendPrintCommand(
                    code.value.queueList[index].size.toString(),
                    index.toString()
                )) {

                showAlert.value = true
            } else {
                showAlertError.value = true
                showAlert.value = true

                code.value.togliPersoneCoda(index)
            }
        } else {
            showAlertError.value = true
            showAlert.value = true
        }
    }

     */
}