package com.example.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.data.model.ConnectionMode
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    fun getLocalIpAddress(context: Context, mode: ConnectionMode): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())

            if (mode == ConnectionMode.USB_TETHER) {
                // Check USB tether interfaces first: rndis0, usb0, usb1
                for (intf in interfaces) {
                    val name = intf.name.lowercase()
                    if (name.contains("rndis") || name.contains("usb") || name.contains("eth")) {
                        val addrs = Collections.list(intf.inetAddresses)
                        for (addr in addrs) {
                            if (!addr.isLoopbackAddress && addr is Inet4Address) {
                                return addr.hostAddress ?: "127.0.0.1"
                            }
                        }
                    }
                }
            } else {
                // Mode is Wi-Fi Local: check wlan0, ap0, etc.
                for (intf in interfaces) {
                    val name = intf.name.lowercase()
                    if (name.contains("wlan") || name.contains("ap") || name.contains("tiwlan")) {
                        val addrs = Collections.list(intf.inetAddresses)
                        for (addr in addrs) {
                            if (!addr.isLoopbackAddress && addr is Inet4Address) {
                                return addr.hostAddress ?: "127.0.0.1"
                            }
                        }
                    }
                }
            }

            // General fallback: any non-loopback site-local IPv4
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }

            "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    fun getNetworkInfo(context: Context): Pair<String, Boolean> {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "") ?: "Local Network"
            val displaySsid = if (ssid.contains("unknown") || ssid.isBlank()) "Wi-Fi LAN" else ssid
            Pair(displaySsid, isWifi)
        } catch (e: Exception) {
            Pair("Local Offline Network", false)
        }
    }
}
