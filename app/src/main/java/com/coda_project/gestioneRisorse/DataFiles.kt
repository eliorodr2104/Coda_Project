package com.coda_project.gestioneRisorse

enum class DataFiles(val valore: String) {
    QUEUE_SIZE_LIST("files_queue/dataFileCode.txt"),

    ADMIN_SETTINGS_APP_USER("files_queue/adminUser.txt"),

    ADMIN_SETTINGS_APP_PASSWORD("files_queue/adminPassword.txt"),

    URL_SERVER_APP("files_queue/urlServer.txt"),

    MAC_ADDRESS_STAMPANTE("files_queue/macAddress.txt"),

    PRINCIPAL_FOLDER_NAME("files_queue")
}