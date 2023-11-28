package com.coda_project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.coda_project.interfacceGrafiche.LoginUI
import com.coda_project.ui.theme.DynamicTheme


class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isBluetoothConnectPermissionGranted = false
    private var isBluetoothAdminPermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        val loginUI = LoginUI(context = this)

        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isBluetoothConnectPermissionGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothConnectPermissionGranted
            isBluetoothAdminPermissionGranted = permissions[Manifest.permission.BLUETOOTH_ADVERTISE] ?: isBluetoothAdminPermissionGranted
        }

        requestPermission()

        setContent {

            DynamicTheme {
                //MenuIniziale()
                loginUI.ViewModeUi()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission(){
        isBluetoothConnectPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        isBluetoothAdminPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isBluetoothConnectPermissionGranted)
            permissionRequest.add(Manifest.permission.BLUETOOTH_CONNECT)

        if (!isBluetoothAdminPermissionGranted)
            permissionRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)

        if (permissionRequest.isNotEmpty())
            permissionLauncher.launch(permissionRequest.toTypedArray())
    }
}