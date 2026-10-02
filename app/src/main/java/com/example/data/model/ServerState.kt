package com.example.data.model

enum class ConnectionMode(val title: String) {
    WIFI("Wi-Fi Local"),
    USB_TETHER("USB / Cable")
}

data class ServerState(
    val isRunning: Boolean = false,
    val ipAddress: String = "127.0.0.1",
    val port: Int = 8888,
    val connectionMode: ConnectionMode = ConnectionMode.WIFI,
    val activePairingPin: String = "",
    val connectedClientsCount: Int = 0,
    val connectedClientDeviceName: String? = null,
    val connectedClientIp: String? = null,
    val isEncryptionEnabled: Boolean = true,
    val isPendingPairRequest: Boolean = false,
    val pendingPairClientName: String = "",
    val pendingPairClientIp: String = ""
) {
    val serverUrl: String
        get() = "http://$ipAddress:$port"
}
