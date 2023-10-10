package com.coda_project

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.compose.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isBluetoothConnectPermissionGranted = false
    private var isBluetoothAdminPermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isBluetoothConnectPermissionGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothConnectPermissionGranted
            isBluetoothAdminPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isBluetoothAdminPermissionGranted
        }

        requestPermission()

        setContent {

            AppTheme {
                MenuIniziale()
            }
        }
    }

    private fun requestPermission(){
        isBluetoothConnectPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        isBluetoothAdminPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isBluetoothConnectPermissionGranted)
            permissionRequest.add(Manifest.permission.BLUETOOTH_CONNECT)

        if (!isBluetoothAdminPermissionGranted)
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionRequest.isNotEmpty())
            permissionLauncher.launch(permissionRequest.toTypedArray())
    }

    private fun readDataFile(
        nameFile: String
    ): String{
        val fileInputStream: FileInputStream //Variabile per leggere il file della memoria di massa

        return try {
            fileInputStream = openFileInput(nameFile)

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
    }

    private fun writeDataFile(
        textToWrite: String,
        nameFile: String
    ): Boolean{
        val fileOutputStream: FileOutputStream //Variabile per scrivere il file nella memoria di massa

        return try {
            fileOutputStream = openFileOutput(nameFile, Context.MODE_PRIVATE)
            fileOutputStream.write(textToWrite.toByteArray())

            true
        }catch (e: Exception){
            false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    /*
    @Preview(showBackground = true,
        device = "spec:parent=7in WSVGA (Tablet),orientation=landscape"
    )
     */
    @Composable
    fun MenuIniziale(){
        // A surface container using the 'background' color from the theme
        //"10:E4:C2:4A:23:E0"
        //"66:32:64:1A:43:45"
        val stampante = remember {
            Stampante("", this)
        }

        val isConfig = remember {
            mutableStateOf(false)
        }

        val httpRequest = HttpRequest(readDataFile(DataFiles.URL_SERVER_APP.name))

        if (readDataFile(DataFiles.MAC_ADDRESS_STAMPANTE.name) == "ERROR" || readDataFile(DataFiles.MAC_ADDRESS_STAMPANTE.name) == ""){
            writeDataFile("", DataFiles.MAC_ADDRESS_STAMPANTE.name)

        }else{
            stampante.indirizzoMacStampante = readDataFile(DataFiles.MAC_ADDRESS_STAMPANTE.name)

            stampante.connectToPrinter()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            if (!isConfig.value){
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent),
                            title = {
                                Text(
                                    "Benvenuto! Seleziona la coda desiderata per ottenere il tuo biglietto!",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    isConfig.value = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        )
                    },
                    content = { innerPadding ->
                        LazyButtons(
                            stampante,
                            innerPadding,
                            httpRequest
                        )
                    }
                )
            }else
                SettingsApp(
                    stampante,
                    httpRequest,
                    isConfig
                )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    //@Preview(device = "spec:width=1880dp,height=1270dp,dpi=240", showBackground = true)
    @Composable
    fun SettingsApp(
        stampante: Stampante,
        httpRequest: HttpRequest,
        isConfig: MutableState<Boolean>
    ){
        //var stampante: Stampante = Stampante("", this)
        //var prova = false

        var queueSize = 0

        if (readDataFile(DataFiles.QUEUE_SIZE_LIST.name) != "ERROR" || readDataFile(DataFiles.QUEUE_SIZE_LIST.name) != ""){
                queueSize = readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt()
        }

        val value = remember { mutableStateOf(queueSize) }

        var inputText by remember { mutableStateOf("") }

        var inputTextMacAddress by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){

            Card(
                modifier = Modifier
                    .padding(15.dp),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ){
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    //Text(text = "Stampante: ${stampante.getConnectionPrinter()}")

                    Text(
                        text = "Configurazione App",
                        modifier = Modifier,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Informazioni app:",
                        modifier = Modifier
                            .padding(start = 10.dp, top = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(15.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 5.dp, top = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stato stampante:",
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = if (stampante.getConnectionPrinter()) "Connessa" else "Non connessa",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(start = 5.dp, top = 2.dp, end = 5.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }


                        //${readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt()}
                        Text(
                            text = "Numero di code sono: ${readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt()}",
                            modifier = Modifier
                                .padding(
                                    start = 5.dp,
                                    top = 10.dp
                                ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "L'URL del server: ${httpRequest.urlServer}",
                            modifier = Modifier
                                .padding(
                                    start = 5.dp,
                                    bottom = 5.dp,
                                    end = 5.dp,
                                    top = 10.dp
                                ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "Modifica app:",
                        modifier = Modifier
                            .padding(start = 10.dp, top = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

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
                            text = "Aumenta il numero di code:",
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
                            text = "Modifica l'URL del server:",
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
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
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
                            text = "Modifica l'indirizzo MAC stampante:",
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

                            label = { Text("Inserisci il nuovo MAC address") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, // Imposta la tastiera per gli URL
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
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

                        Button(
                            onClick = {
                                writeDataFile(value.value.toString(), DataFiles.QUEUE_SIZE_LIST.name)

                                if (inputText != "") {
                                    httpRequest.urlServer = inputText

                                    writeDataFile(inputText, DataFiles.URL_SERVER_APP.name)

                                }else
                                    httpRequest.urlServer = readDataFile(DataFiles.URL_SERVER_APP.name)


                                if (inputTextMacAddress != ""){
                                    stampante.indirizzoMacStampante = inputTextMacAddress

                                    stampante.connectToPrinter()

                                    writeDataFile(stampante.indirizzoMacStampante, DataFiles.MAC_ADDRESS_STAMPANTE.name)

                                }else{
                                    stampante.indirizzoMacStampante = readDataFile(DataFiles.MAC_ADDRESS_STAMPANTE.name)
                                }


                                isConfig.value = false
                            },
                            modifier = Modifier
                                .padding(top = 10.dp, start = 10.dp, bottom = 10.dp)
                        ) {
                            Text(text = "Avanti")
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

    @Composable
    fun AlertMessageBox(
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun LazyButtons(
        stampante: Stampante,
        contentPadding: PaddingValues,
        httpRequest: HttpRequest
    ) {
        val isSelected = rememberSaveable {
            mutableStateOf(false)
        }

        var showAlert by rememberSaveable { mutableStateOf(false) }
        var showAlertPrinter by remember { mutableStateOf(false) }

        val dateFormat = SimpleDateFormat("dd MMMM 'del' yyyy", Locale.getDefault())
        val currentDate = Date.from(Instant.now())
        val formattedDate = dateFormat.format(currentDate)

        val code = remember {
            mutableStateOf(GestioneCode())
        }

        val coroutineScope = rememberCoroutineScope()

        val controllo = remember {
            mutableStateOf(false)
        }

        var isRequestInProgress by remember { mutableStateOf(false) }

        // Conto alla rovescia per cambiare automaticamente il pulsante dopo 3 secondi
        LaunchedEffect(showAlert) {
            if (showAlert) {
                delay(5000) // Attendiamo 3 secondi

                showAlert = false
            }
        }

        if (!controllo.value){
            if (readDataFile(DataFiles.QUEUE_SIZE_LIST.name) == "ERROR" || readDataFile(DataFiles.QUEUE_SIZE_LIST.name) == ""){
                writeDataFile("1", DataFiles.QUEUE_SIZE_LIST.name)

                code.value.aggiungiCode(readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt())
            }else{
                code.value.aggiungiCode(readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt())
            }

            controllo.value = true

        }else
            code.value.aggiungiCode(readDataFile(DataFiles.QUEUE_SIZE_LIST.name).toInt())

        if (!showAlert){
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                contentPadding = PaddingValues(all = 15.dp),
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 65.dp, bottom = 10.dp)
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = true
            ) {
                items(code.value.queueList.size) { index ->
                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier
                            .fillMaxWidth()

                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    ){
                        // Creazione di un elemento in base all'indice
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor =
                                if (isSelected.value)
                                    MaterialTheme.colorScheme.primaryContainer

                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier
                                .padding(
                                    end = 4.dp,
                                )
                                .border(
                                    width = 3.5.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable(onClick = {
                                    isSelected.value = isSelected.value != true

                                    if (!isRequestInProgress) {

                                        isRequestInProgress = true

                                        // Esempio di chiamata a una funzione asincrona
                                        coroutineScope.launch(Dispatchers.IO) {
                                            // Chiamata a funzione asincrona con 'suspend'

                                            if (httpRequest.getAggiornamentoCoda(index.toString()) == "200") {

                                                code.value.aumentaCoda(index)

                                                if (stampante.sendPrintCommand(
                                                        """
                                            ----------------------------------
                                            ---          Ticket            ---
                                            ---         ${code.value.queueList[index].size}            ---
                                            ----------------------------------
                                            """.trimIndent()
                                                    )
                                                ) {

                                                    showAlert = true

                                                } else {
                                                    showAlertPrinter = true
                                                    showAlert = true

                                                    code.value.togliPersoneCoda(index)

                                                    Thread.sleep(500)
                                                }
                                            } else {
                                                showAlertPrinter = true
                                                showAlert = true

                                            }

                                            isRequestInProgress = false
                                        }
                                    }

                                    //httpRequest.updateQueueStatus(index)
                                }),
                            shape = RoundedCornerShape(20.dp)
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
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Column for the left-aligned "Coda" text
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
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
                                    }
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
                            }
                        }
                    }
                }
            }

        }else {
            AlertMessageBox(
                title = if (!showAlertPrinter) "Operazione conclusa!" else "Errore!",
                message = if (!showAlertPrinter) "Per favore, ritiri il biglietto" else "Siamo spiacenti, ma si è verificato un errore nell'operazione.\n" +
                        "Si prega di contattare l'assistenza immediatamente. \n" +
                        "Ci scusiamo per il disagio.",
                onDismiss = {
                    showAlert = false
                    showAlertPrinter = false

                    controllo.value = false
                }
            )
        }
    }

}