package com.coda_project.interfacceGrafiche

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.coda_project.R
import com.coda_project.gestioneConnessioni.HttpRequest
import com.coda_project.gestioneConnessioni.Stampante
import com.coda_project.gestioneRisorse.DataFiles
import com.coda_project.gestioneRisorse.GestioneCode
import com.coda_project.gestioneRisorse.GestioneRisorse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
class LoginUI(context: Context) {

    private val guessUI = GuestUI(context = context)
    private val adminUI = AdminUI(context = context)

    private val gestioneRisorse = GestioneRisorse(context = context)

    private val httpRequest = HttpRequest(gestioneRisorse.readDataFile(DataFiles.URL_SERVER_APP.valore))

    private val nameStampante = gestioneRisorse.readDataFile(DataFiles.MAC_ADDRESS_STAMPANTE.valore)

    private val stampante: Stampante

    init {
        if (nameStampante == "ERROR" || nameStampante == ""){
            gestioneRisorse.writeDataFile("", DataFiles.MAC_ADDRESS_STAMPANTE.valore)

            stampante = Stampante("", context = context)
        }else{
            stampante = Stampante(nameStampante, context = context)

            stampante.findBT()
            stampante.connectToPrinter()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ViewModeUi(){
        val code = remember { mutableStateOf(GestioneCode()) }

        val indexReadQueueList = gestioneRisorse.readDataFile(DataFiles.QUEUE_SIZE_LIST.valore)

        if (code.value.queueList.isEmpty()){
            if (indexReadQueueList == "ERROR" || indexReadQueueList == ""){
                gestioneRisorse.writeDataFile("1", DataFiles.QUEUE_SIZE_LIST.valore)

                code.value.aggiungiCode(indexReadQueueList.toInt())

            }else{
                code.value.aggiungiCode(indexReadQueueList.toInt())
            }
        }

        val adminUser = gestioneRisorse.readDataFile(DataFiles.ADMIN_SETTINGS_APP_USER.valore)
        val adminPassword = gestioneRisorse.readDataFile(DataFiles.ADMIN_SETTINGS_APP_PASSWORD.valore)

        val selectView = remember {
            mutableStateOf(0) //-1 Versione vecchia
        }

        val isConfig = remember {
            mutableStateOf(false)
        }

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent),
                        title = {
                            Text(
                                when(selectView.value){
                                    -1 -> ""
                                    0 -> ""
                                    1 -> "Sportelli"
                                    2 -> "Sportelli menu"
                                    else -> {""}
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                },
                floatingActionButton = {
                    if (selectView.value == 2){
                        ExtendedFloatingActionButton(
                            onClick = { isConfig.value = !isConfig.value },
                            icon = { Icon(Icons.Filled.Settings, "Extended floating action button.") },
                            text = { Text(text = "Impostazioni") },
                        )
                    }
                },
                content = { innerPadding ->
                    when(selectView.value){
                        0 -> LoginView(
                            paddingValues = innerPadding,
                            indexForChangeView = selectView
                        )
                        1 -> guessUI.MenuIniziale(
                            contentPadding = innerPadding,
                            stampante = stampante,
                            httpRequest = httpRequest,
                            code = code
                        )
                        2 -> adminUI.MenuIniziale(
                            stampante = stampante,
                            httpRequest = httpRequest,
                            isConfig = isConfig,
                            innerPadding = innerPadding,
                            code = code
                        )
                        //ADMIN -> MODIFIER THE METHOD
                    }

                    /*
                    if ((adminUser != "" && adminPassword != "") || selectView.value > -1){
                        //Login RESULT -> WHEN: (OUTPUT->LOGIN) SELECT GUESS || ADMIN

                        //LOGIN VIEW
                        when(selectView.value){
                            0 -> LoginView(
                                paddingValues = innerPadding,
                                indexForChangeView = selectView
                            )
                            1 -> guessUI.MenuIniziale(
                                contentPadding = innerPadding,
                                stampante = stampante,
                                httpRequest = httpRequest,
                                code = code
                            )
                            2 -> adminUI.MenuIniziale(
                                stampante = stampante,
                                httpRequest = httpRequest,
                                isConfig = isConfig,
                                innerPadding = innerPadding,
                                code = code
                            )
                            //ADMIN -> MODIFIER THE METHOD
                        }

                    }else{
                        RegisterAdminCredentials(
                            paddingValues = innerPadding,
                            indexForChangeView = selectView
                        )
                    }

                     */

                    /*
                    if (!isConfig.value){
                        LazyButtons(
                            stampante,
                            innerPadding,
                            httpRequest
                        )

                    }else
                        SettingsApp(
                            stampante,
                            httpRequest,
                            isConfig,
                            innerPadding
                        )

                     */
                }
            )
        }
    }

    @Composable
    private fun LoginView(
        paddingValues: PaddingValues,
        indexForChangeView: MutableState<Int>
    ){
        var usernameAdmin by remember {
            mutableStateOf("")
        }

        var passwordAdmin by remember {
            mutableStateOf("")
        }

        var indexForHiddenMenu by remember {
            mutableStateOf(0)
        }

        val stateForHiddenMenu = remember {
            mutableStateOf(false)
        }

        var passwordVisibility by remember {
            mutableStateOf(false)
        }

        var isErrorAdmin by remember { mutableStateOf(false) } // Inizialmente, nessun errore
        var isErrorPassword by remember { mutableStateOf(false) } // Inizialmente, nessun errore

        var moved by remember { mutableStateOf(false) }
        val pxToMove = with(LocalDensity.current) {
            -150.dp.toPx().roundToInt()
        }
        val offset by animateIntOffsetAsState(
            targetValue = if (moved) {
                IntOffset(0, pxToMove)
            } else {
                IntOffset.Zero
            },
            label = "offset"
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .height(if (moved) LocalConfiguration.current.screenHeightDp.dp else 100.dp)
                        .background(Color.Cyan)

                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Queue",
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            text = "Applicazione per la gestione delle code",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(
                verticalArrangement = if (!moved) Arrangement.Bottom else Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    //.animateContentSize()
                    .height(if (moved) 400.dp else 200.dp)
                    .background(color = Color.Red)
            ) {
                AnimatedVisibility(
                    visible = !moved,
                    enter = fadeIn(
                        // Overwrites the initial value of alpha to 0.4f for fade in, 0 by default
                        initialAlpha = 0.5f
                    ),
                    exit = fadeOut(
                        // Overwrites the default animation with tween
                        animationSpec = tween(durationMillis = 100, delayMillis = 450)
                    )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
                                .height(45.dp),
                            onClick = {
                                moved = !moved
                            }
                        ) {
                            Text(text = "Admin menu")
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 30.dp, end = 30.dp, bottom = 25.dp)
                                .height(45.dp),
                            onClick = {
                            }
                        ) {
                            Text(text = "Guest menu")
                        }

                        ClickableText(
                            text = AnnotatedString("Admin credentials?"),
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                            onClick = {

                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = moved,
                    enter = fadeIn(
                        // Overwrites the initial value of alpha to 0.4f for fade in, 0 by default
                        animationSpec = tween(durationMillis = 100, delayMillis = 300)
                    ),
                    exit = fadeOut(
                        // Overwrites the default animation with tween
                        animationSpec = tween(durationMillis = 100)
                    )
                ){
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                        ) {
                            TextField(
                                value = usernameAdmin,
                                onValueChange = {
                                    usernameAdmin = it
                                },
                                label = { Text("Inserisci username") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorAdmin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    )
                            )

                            if (isErrorAdmin)
                                Text(
                                    text = "Admin non valido",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                )
                        }

                        Column {
                            TextField(
                                value = passwordAdmin,
                                onValueChange = {
                                    passwordAdmin = it
                                },
                                label = { Text("Inserisci password") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorPassword,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    ),

                                trailingIcon = {
                                    IconButton(onClick = {
                                        passwordVisibility = !passwordVisibility
                                    }) {
                                        if (passwordVisibility){
                                            Icon(Icons.Filled.Visibility, "Extended floating action button.")

                                        }else
                                            Icon(Icons.Filled.VisibilityOff, "Extended floating action button.")
                                    }
                                },
                                visualTransformation = if (passwordVisibility) VisualTransformation.None else
                                    PasswordVisualTransformation()
                            )


                            if (isErrorPassword)
                                Text(
                                    text = "Password non valida",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                )
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
                                .height(45.dp),
                            onClick = {
                                moved = !moved
                            }
                        ) {
                            Text(text = "PROVANCSACHAIEHCEAHIH")
                        }
                    }
                }

                /*
                AnimatedVisibility(
                    visible = moved,
                    enter = fadeIn(
                        // Overwrites the initial value of alpha to 0.4f for fade in, 0 by default
                        initialAlpha = 0.4f
                    ),
                    exit = fadeOut(
                        // Overwrites the default animation with tween
                        animationSpec = tween(durationMillis = 350)
                    )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
                                .height(45.dp),
                            onClick = {
                                moved = !moved
                            }
                        ) {
                            Text(text = "PROVANCSACHAIEHCEAHIH")
                        }
                    }
                }

                 */
            }
        }

        /*
        Column(
            modifier = Modifier
                .fillMaxSize()
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    "Circle admin",
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                )

                Text(
                    text = "Benvenuto!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 35.dp,
                    topEnd = 35.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, top = 5.dp)
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(bottom = 25.dp)
                                .clickable { if (indexForHiddenMenu < 3) indexForHiddenMenu++ },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                        ) {
                            TextField(
                                value = usernameAdmin,
                                onValueChange = {
                                    usernameAdmin = it
                                },
                                label = { Text("Inserisci username") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorAdmin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    )
                            )

                            if (isErrorAdmin)
                                Text(
                                    text = "Admin non valido",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                )
                        }

                        Column {
                            TextField(
                                value = passwordAdmin,
                                onValueChange = {
                                    passwordAdmin = it
                                },
                                label = { Text("Inserisci password") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorPassword,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    ),

                                trailingIcon = {
                                    IconButton(onClick = {
                                        passwordVisibility = !passwordVisibility
                                    }) {
                                        if (passwordVisibility){
                                            Icon(Icons.Filled.Visibility, "Extended floating action button.")

                                        }else
                                            Icon(Icons.Filled.VisibilityOff, "Extended floating action button.")
                                    }
                                },
                                visualTransformation = if (passwordVisibility) VisualTransformation.None else
                                    PasswordVisualTransformation()
                            )


                            if (isErrorPassword)
                                Text(
                                    text = "Password non valida",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Button(
                                onClick = {
                                    if (usernameAdmin == "" && passwordAdmin == ""){
                                        isErrorPassword = true
                                        isErrorAdmin = true

                                    }else {
                                        isErrorPassword = false
                                        isErrorAdmin = false
                                    }

                                    if (usernameAdmin != ""){
                                        if (passwordAdmin != ""){
                                            /*
                                                val usernameAdminFile = gestioneRisorse.readDataFile(DataFiles.ADMIN_SETTINGS_APP_USER.valore)
                                                val passwordAdminFile = gestioneRisorse.readDataFile(DataFiles.ADMIN_SETTINGS_APP_PASSWORD.valore)



                                                if (usernameAdmin == usernameAdminFile && passwordAdmin == passwordAdminFile){
                                                    indexForChangeView.value = 2

                                                }else
                                                    isError = true

                                             */

                                            indexForChangeView.value = 2
                                        }else
                                            isErrorPassword = true

                                    }else
                                        isErrorAdmin = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = if (!stateForHiddenMenu.value) "Login" else "admin")
                            }
                        }

                        if (indexForHiddenMenu == 3) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Button(
                                    onClick = {
                                              indexForChangeView.value = 1
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "guest")
                                }
                            }
                        }
                    }


                }
            }
        }

         */
    }


    @Composable
    private fun RegisterAdminCredentials(
        paddingValues: PaddingValues,
        indexForChangeView: MutableState<Int>
    ){
        var usernameAdmin by remember {
            mutableStateOf("")
        }

        var passwordAdmin by remember {
            mutableStateOf("")
        }

        var passwordVisibility by remember {
            mutableStateOf(false)
        }

        var isErrorAdmin by remember { mutableStateOf(false) } // Inizialmente, nessun errore
        var isErrorPassword by remember { mutableStateOf(false) } // Inizialmente, nessun errore

        Column(
            modifier = Modifier
                .fillMaxSize()
        ){

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    "Circle admin",
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                )

                Text(
                    text = "Benvenuto!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 35.dp,
                    topEnd = 35.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, top = 5.dp)
                    ) {
                        Text(
                            text = "Registrazione Admin",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(bottom = 25.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                        ) {
                            TextField(
                                value = usernameAdmin,
                                onValueChange = {
                                    usernameAdmin = it
                                },
                                label = { Text("Username") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorAdmin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    )
                            )

                            if (isErrorAdmin)
                                Text(
                                    text = "Admin non valido",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 20.dp)

                                )
                        }

                        Column(
                            modifier = Modifier
                                .padding(bottom = 25.dp)
                        ) {
                            TextField(
                                value = passwordAdmin,
                                onValueChange = {
                                    passwordAdmin = it
                                },
                                label = { Text("Password") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password, // Imposta la tastiera per gli URL
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                singleLine = true,
                                isError = isErrorPassword,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp
                                    ),

                                trailingIcon = {
                                    IconButton(onClick = {
                                        passwordVisibility = !passwordVisibility
                                    }) {
                                        if (passwordVisibility){
                                            Icon(Icons.Filled.Visibility, "Extended floating action button.")

                                        }else
                                            Icon(Icons.Filled.VisibilityOff, "Extended floating action button.")
                                    }
                                },
                                visualTransformation = if (passwordVisibility) VisualTransformation.None else
                                    PasswordVisualTransformation()
                            )

                            if (isErrorPassword)
                                Text(
                                    text = "Password non valida",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 20.dp)
                                )
                        }
                    }

                    Button(
                        onClick = {
                            if (usernameAdmin == "" && passwordAdmin == ""){
                                isErrorPassword = true
                                isErrorAdmin = true

                            }else{
                                isErrorPassword = false
                                isErrorAdmin = false
                            }

                            if (usernameAdmin != ""){
                                if (passwordAdmin != "" && passwordAdmin.length > 7){
                                    /*
                                    if (
                                        gestioneRisorse.writeDataFile(usernameAdmin, DataFiles.ADMIN_SETTINGS_APP_USER.valore)
                                        &&
                                        gestioneRisorse.writeDataFile(passwordAdmin, DataFiles.ADMIN_SETTINGS_APP_PASSWORD.valore)
                                    ){
                                        indexForChangeView.value = 0
                                    }

                                     */
                                    indexForChangeView.value = 0
                                }else
                                    isErrorPassword = true
                            }else
                                isErrorAdmin = true

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Register",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}