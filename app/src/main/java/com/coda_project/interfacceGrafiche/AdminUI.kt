package com.coda_project.interfacceGrafiche

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coda_project.R
import com.coda_project.gestioneConnessioni.HttpRequest
import com.coda_project.gestioneConnessioni.Stampante
import com.coda_project.gestioneRisorse.DataFiles
import com.coda_project.gestioneRisorse.GestioneCode
import com.coda_project.gestioneRisorse.GestioneRisorse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class AdminUI(context: Context) {
    private var gestioneRisorse = GestioneRisorse(context = context)

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MenuIniziale(
        stampante: Stampante,
        httpRequest: HttpRequest,
        isConfig: MutableState<Boolean>,
        innerPadding: PaddingValues,
        code: MutableState<GestioneCode>
    ){

        GestioneSportelli(
            httpRequest = httpRequest,
            innerPadding = innerPadding,
            coda = code.value
        )

        val sheetState = rememberModalBottomSheetState()

        if (isConfig.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    isConfig.value = false
                },
                sheetState = sheetState
            ) {
                SettingsApp(
                    stampante = stampante,
                    httpRequest = httpRequest,
                    isConfig = isConfig,
                    innerPadding = innerPadding
                )
            }
        }

        /*
        if (!isConfig.value)
            GestioneSportelli(
                httpRequest = httpRequest,
                innerPadding = innerPadding,
                sizeCode = code.value.queueList.size
            )

        else
            SettingsApp(
                stampante = stampante,
                httpRequest = httpRequest,
                isConfig = isConfig,
                innerPadding = innerPadding)

         */
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun GestioneSportelli(
        httpRequest: HttpRequest,
        innerPadding: PaddingValues,
        coda: GestioneCode
    ){
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date.from(Instant.now())
        val formattedDate = dateFormat.format(currentDate)

        val coroutineScope = rememberCoroutineScope()
        var shouldReloadList by remember { mutableStateOf(true) }
        var showAlert by remember { mutableStateOf(false) }
        var showAlertErrorBound by remember { mutableStateOf(false) }


        val code by remember {
            mutableStateOf(coda)
        }

        val numberMaxCode by remember {
            mutableStateOf(ArrayList<Int>())
        }

        LaunchedEffect(true) {
            // Avvio del thread che si avvia all'avvio della view
            val thread = launch {
                while (true) {
                    if (numberMaxCode.isNotEmpty())
                        numberMaxCode.clear()

                    for (i in 0 until coda.queueList.size) {
                        numberMaxCode.add(httpRequest.downloadLinkedList(i.toString()).size)
                    }

                    shouldReloadList = false

                    delay(5000) // Attendiamo 5 secondi prima di eseguire di nuovo l'operazione
                }
            }
        }
        if (showAlert)
            AlertMessageBox(
                title = if (!showAlertErrorBound) "Coda!" else "Error!",
                message = if (!showAlertErrorBound) "Persona mandata avanti nella coda con successo." else "Non ci sono persone da servire nella coda.",
                onDismiss = {
                    showAlert = false
                    showAlertErrorBound = false
                }
            )

        else {
            if (shouldReloadList){
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(200.dp),
                    contentPadding = PaddingValues(all = 15.dp),
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ){
                    items(code.queueList.size){ index ->

                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            border = BorderStroke(4.dp, Color.Black),
                            modifier = Modifier
                                .clickable(onClick = {
                                    coroutineScope.launch(Dispatchers.IO){
                                        if (code.queueList[index].size <= httpRequest.downloadLinkedList(index.toString()).size){
                                            code.aumentaCoda(index)

                                            showAlert = true
                                            //Mandare al server per essere visualizzato nello schermo

                                        }else {
                                            showAlert = true
                                            showAlertErrorBound = true
                                        }

                                        delay(2000)
                                    }

                                    shouldReloadList = false
                                }),
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .padding(
                                        start = 15.dp,
                                        top = 10.dp
                                    ),
                                softWrap = false
                            )

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
                                        painter = painterResource(R.drawable.icons8_queue_48___), // Replace R.drawable.my_image with your drawable resource ID
                                        contentDescription = "My Image",
                                        modifier = Modifier
                                            .size(55.dp) // Set the size of the image
                                            .padding(
                                                start = 25.dp
                                            )
                                    )

                                    Text(
                                        text = "Nro: $index",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(
                                            top = 13.dp,
                                            start = 5.dp
                                        ),
                                        softWrap = false
                                    )
                                }

                                Text(
                                    text = "Numero della persona a servire:",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(start = 25.dp, top = 10.dp)
                                )

                                Text(
                                    text = (code.queueList[index].size).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(start = 25.dp, bottom = 15.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(
                                        start = 25.dp,
                                        bottom = 10.dp
                                    )
                                ) {

                                    Text(
                                        text = "Persone nella coda: ${if (numberMaxCode.isNotEmpty()) numberMaxCode[index] + 1 else 0}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Start,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }else{
                shouldReloadList = true
            }
        }
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
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }


    @Composable
    fun SettingsApp(
        stampante: Stampante,
        httpRequest: HttpRequest,
        isConfig: MutableState<Boolean>,
        innerPadding: PaddingValues
    ){

        val queueListSize = gestioneRisorse.readDataFile(DataFiles.QUEUE_SIZE_LIST.valore).toInt()

        val value = remember { mutableStateOf(queueListSize) }

        var inputText by remember { mutableStateOf("") }

        var inputTextMacAddress by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ){

                Text(
                    text = "Informazioni app:",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                    ) {
                        Text(
                            text = "Stato stampante:",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "N.code: $queueListSize",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "URL server: ${httpRequest.urlServer}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = "Modifica app:",
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(15.dp)
                            )
                    ){
                        Text(
                            text = "Modifica il numero di code:",
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        NumberCounter(number = value)

                        Text(
                            text = "Modifica URL server:",
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    top = 10.dp
                                ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        TextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                            },
                            modifier = Modifier
                                .padding(start = 5.dp, end = 5.dp),

                            label = { Text("Inserisci url") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri, // Imposta la tastiera per gli URL
                                imeAction = ImeAction.Done
                            ),
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiaryContainer),
                            shape = RoundedCornerShape(100.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Text(
                            text = "Modifica nome stampante:",
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    top = 10.dp,
                                    end = 5.dp
                                ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        TextField(
                            value = inputTextMacAddress,
                            onValueChange = {
                                inputTextMacAddress = it
                            },
                            modifier = Modifier
                                .padding(start = 5.dp, end = 5.dp),

                            label = { Text("Inserisci nome stampante") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, //
                                imeAction = ImeAction.Done
                            ),
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiaryContainer),
                            shape = RoundedCornerShape(100.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            if (!isConfig.value){
                                gestioneRisorse.writeDataFile(value.value.toString(), DataFiles.QUEUE_SIZE_LIST.valore)

                                if (inputText != "") {
                                    httpRequest.urlServer = inputText

                                    gestioneRisorse.writeDataFile(inputText, DataFiles.URL_SERVER_APP.valore)

                                }else
                                    httpRequest.urlServer = gestioneRisorse.readDataFile(
                                        DataFiles.URL_SERVER_APP.valore)


                                if (inputTextMacAddress != ""){
                                    stampante.indirizzoMacStampante = inputTextMacAddress

                                    stampante.findBT()
                                    stampante.connectToPrinter()

                                    gestioneRisorse.writeDataFile(stampante.indirizzoMacStampante, DataFiles.MAC_ADDRESS_STAMPANTE.valore)

                                }else{
                                    stampante.indirizzoMacStampante = gestioneRisorse.readDataFile(
                                        DataFiles.MAC_ADDRESS_STAMPANTE.valore)
                                }
                            }

                            /*
                            Button(
                                onClick = {
                                    gestioneRisorse.writeDataFile(value.value.toString(), DataFiles.QUEUE_SIZE_LIST.valore)

                                    if (inputText != "") {
                                        httpRequest.urlServer = inputText

                                        gestioneRisorse.writeDataFile(inputText, DataFiles.URL_SERVER_APP.valore)

                                    }else
                                        httpRequest.urlServer = gestioneRisorse.readDataFile(
                                            DataFiles.URL_SERVER_APP.valore)


                                    if (inputTextMacAddress != ""){
                                        stampante.indirizzoMacStampante = inputTextMacAddress

                                        stampante.findBT()
                                        stampante.connectToPrinter()

                                        gestioneRisorse.writeDataFile(stampante.indirizzoMacStampante, DataFiles.MAC_ADDRESS_STAMPANTE.valore)

                                    }else{
                                        stampante.indirizzoMacStampante = gestioneRisorse.readDataFile(
                                            DataFiles.MAC_ADDRESS_STAMPANTE.valore)
                                    }

                                    isConfig.value = false
                                },
                            ) {
                                Text(text = "Modifica")
                            }

                             */



                            Button(onClick = {
                                stampante.findBT()
                                stampante.connectToPrinter()
                            }) {
                                Text(text = "Refresh BT")
                            }

                            /*
                            IconButton(onClick = {
                                stampante.findBT()
                                stampante.connectToPrinter()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }

                             */
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun NumberCounter(
        number: MutableState<Int>
    ){
        Card(
            modifier = Modifier
                .padding(start = 5.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically // Per centrare verticalmente il contenuto
            ) {
                Button(
                    onClick = { number.value = if (number.value - 1 >= 0) number.value - 1 else 0 },
                    modifier = Modifier.background(Color.Transparent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    // Contenuto del pulsante
                    Icon(
                        Icons.Default.ArrowBack,
                        "",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Text(
                    text = number.value.toString(),
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Button(
                    onClick = { number.value = number.value + 1 },
                    modifier = Modifier.background(Color.Transparent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        "",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}