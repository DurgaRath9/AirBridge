package com.example.server

import android.content.Context
import android.net.Uri
import com.example.crypto.CryptoUtils
import com.example.data.model.ConnectionMode
import com.example.data.model.MediaFileItem
import com.example.data.model.ServerState
import com.example.data.model.TransferDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AirBridgeServer(
    private val context: Context,
    private val onStateChanged: (ServerState) -> Unit,
    private val onTransferProgress: (
        fileName: String,
        bytesTransferred: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        direction: TransferDirection
    ) -> Unit,
    private val onTransferCompleted: (fileName: String, bytes: Long, sha256: String, direction: TransferDirection) -> Unit,
    private val onTransferFailed: (fileName: String, reason: String) -> Unit,
    private val onDevicePaired: (deviceName: String, clientIp: String) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    var currentState: ServerState = ServerState()
        private set

    @Volatile
    var isTransferPaused: Boolean = false

    @Volatile
    var isTransferCancelled: Boolean = false

    private val authorizedTokens = ConcurrentHashMap<String, String>() // token -> clientName
    var fileProvider: () -> List<MediaFileItem> = { emptyList() }

    fun start(port: Int = 8888, mode: ConnectionMode = ConnectionMode.WIFI) {
        if (currentState.isRunning) return

        val pin = CryptoUtils.generatePairingPin()
        val ip = NetworkUtils.getLocalIpAddress(context, mode)

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                currentState = currentState.copy(
                    isRunning = true,
                    ipAddress = ip,
                    port = port,
                    connectionMode = mode,
                    activePairingPin = pin
                )
                onStateChanged(currentState)

                while (isActive && serverSocket?.isClosed == false) {
                    val socket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        null
                    }
                    if (socket != null) {
                        scope.launch {
                            handleClientSocket(socket)
                        }
                    }
                }
            } catch (e: Exception) {
                stop()
            }
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        currentState = currentState.copy(
            isRunning = false,
            connectedClientsCount = 0,
            connectedClientDeviceName = null
        )
        onStateChanged(currentState)
    }

    private fun handleClientSocket(socket: Socket) {
        val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
        try {
            val input = BufferedInputStream(socket.getInputStream())
            val output = BufferedOutputStream(socket.getOutputStream())

            val reader = BufferedReader(InputStreamReader(input))
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return

            val method = parts[0]
            val fullPath = parts[1]
            val urlPath = fullPath.substringBefore("?")
            val queryString = if (fullPath.contains("?")) fullPath.substringAfter("?") else ""
            val queryParams = parseQueryParams(queryString)

            // Read headers
            val headers = mutableMapOf<String, String>()
            var headerLine: String?
            while (reader.readLine().also { headerLine = it } != null) {
                if (headerLine.isNullOrBlank()) break
                val colonIdx = headerLine!!.indexOf(":")
                if (colonIdx > 0) {
                    val k = headerLine!!.substring(0, colonIdx).trim().lowercase()
                    val v = headerLine!!.substring(colonIdx + 1).trim()
                    headers[k] = v
                }
            }

            // Routing
            when {
                urlPath == "/" || urlPath == "/index.html" -> {
                    servePortalHtml(output)
                }

                urlPath == "/api/status" -> {
                    serveStatus(output)
                }

                urlPath == "/api/pair" && method.equals("POST", ignoreCase = true) -> {
                    handlePair(reader, headers, clientIp, output)
                }

                urlPath == "/api/files" -> {
                    serveFilesList(output)
                }

                urlPath == "/api/download" -> {
                    val fileId = queryParams["id"]
                    val token = queryParams["token"] ?: headers["authorization"]?.removePrefix("Bearer ")?.trim()
                    if (isAuthorized(token)) {
                        handleDownload(fileId, headers, output)
                    } else {
                        sendUnauthorized(output)
                    }
                }

                urlPath == "/api/backup/bundle" -> {
                    val type = queryParams["type"] ?: "incremental"
                    val token = queryParams["token"] ?: headers["authorization"]?.removePrefix("Bearer ")?.trim()
                    if (isAuthorized(token)) {
                        handleBackupBundle(type, output)
                    } else {
                        sendUnauthorized(output)
                    }
                }

                urlPath == "/api/upload" && method.equals("POST", ignoreCase = true) -> {
                    val token = headers["authorization"]?.removePrefix("Bearer ")?.trim()
                    if (isAuthorized(token)) {
                        handleUpload(input, headers, output)
                    } else {
                        sendUnauthorized(output)
                    }
                }

                else -> {
                    sendNotFound(output)
                }
            }
        } catch (_: Exception) {
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun isAuthorized(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return authorizedTokens.containsKey(token)
    }

    private fun servePortalHtml(out: OutputStream) {
        val deviceName = android.os.Build.MODEL ?: "Android Phone"
        val html = DesktopWebPortalHtml.getPortalHtml(deviceName, currentState.port)
        val bytes = html.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun serveStatus(out: OutputStream) {
        val statusJson = JSONObject().apply {
            put("phoneName", android.os.Build.MODEL ?: "Android Phone")
            put("mode", currentState.connectionMode.name)
            put("isEncrypted", currentState.isEncryptionEnabled)
            put("serverPort", currentState.port)
            put("connectedClients", currentState.connectedClientsCount)
        }
        sendJsonResponse(out, statusJson.toString())
    }

    private fun handlePair(reader: BufferedReader, headers: Map<String, String>, clientIp: String, out: OutputStream) {
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        val bodyChars = CharArray(contentLength)
        reader.read(bodyChars, 0, contentLength)
        val body = String(bodyChars)

        val json = try { JSONObject(body) } catch (e: Exception) { JSONObject() }
        val pin = json.optString("pin", "")
        val clientDevice = json.optString("deviceName", "Laptop")

        if (pin == currentState.activePairingPin) {
            val token = CryptoUtils.generateSessionToken()
            authorizedTokens[token] = clientDevice
            currentState = currentState.copy(
                connectedClientsCount = authorizedTokens.size,
                connectedClientDeviceName = clientDevice,
                connectedClientIp = clientIp
            )
            onStateChanged(currentState)
            onDevicePaired(clientDevice, clientIp)

            val resp = JSONObject().apply {
                put("success", true)
                put("token", token)
                put("deviceName", android.os.Build.MODEL ?: "Android Phone")
            }
            sendJsonResponse(out, resp.toString())
        } else {
            val resp = JSONObject().apply {
                put("success", false)
                put("error", "Invalid PIN")
            }
            sendJsonResponse(out, resp.toString(), 401)
        }
    }

    private fun serveFilesList(out: OutputStream) {
        val files = fileProvider()
        val array = JSONArray()
        for (f in files) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("size", f.size)
                put("category", f.category.name)
                put("mimeType", f.mimeType)
                put("lastModified", f.lastModified)
                put("sha256", f.sha256)
                put("syncStatus", f.syncStatus.name)
            }
            array.put(obj)
        }
        sendJsonResponse(out, array.toString())
    }

    private fun handleDownload(fileId: String?, headers: Map<String, String>, out: OutputStream) {
        if (fileId == null) {
            sendNotFound(out)
            return
        }
        val file = fileProvider().find { it.id == fileId } ?: run {
            sendNotFound(out)
            return
        }

        val fileInputStream: InputStream? = try {
            if (file.uriString.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(file.uriString))
            } else {
                File(file.path).takeIf { it.exists() }?.let { FileInputStream(it) }
            }
        } catch (e: Exception) {
            null
        }

        if (fileInputStream == null) {
            sendNotFound(out)
            return
        }

        val totalSize = file.size
        val rangeHeader = headers["range"]
        var startByte = 0L
        var endByte = totalSize - 1

        val isPartial = if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            val ranges = rangeHeader.removePrefix("bytes=").split("-")
            startByte = ranges.getOrNull(0)?.toLongOrNull() ?: 0L
            if (ranges.size > 1 && ranges[1].isNotBlank()) {
                endByte = ranges[1].toLongOrNull() ?: (totalSize - 1)
            }
            true
        } else false

        val contentLength = endByte - startByte + 1
        val statusLine = if (isPartial) "HTTP/1.1 206 Partial Content" else "HTTP/1.1 200 OK"
        val responseHeaders = StringBuilder().apply {
            append("$statusLine\r\n")
            append("Content-Type: ${file.mimeType.ifBlank { "application/octet-stream" }}\r\n")
            append("Content-Length: $contentLength\r\n")
            append("Accept-Ranges: bytes\r\n")
            if (isPartial) {
                append("Content-Range: bytes $startByte-$endByte/$totalSize\r\n")
            }
            if (file.sha256.isNotBlank()) {
                append("ETag: \"${file.sha256}\"\r\n")
            }
            append("Content-Disposition: attachment; filename=\"${file.name}\"\r\n")
            append("Connection: close\r\n\r\n")
        }.toString()

        out.write(responseHeaders.toByteArray())

        // Stream data with speed calculation & pause/resume checks
        val buffer = ByteArray(64 * 1024)
        var transferred = 0L
        var lastTime = System.currentTimeMillis()
        var bytesSinceLastTime = 0L

        fileInputStream.use { input ->
            if (startByte > 0) {
                input.skip(startByte)
            }
            while (transferred < contentLength) {
                while (isTransferPaused) {
                    Thread.sleep(100)
                }
                if (isTransferCancelled) {
                    onTransferFailed(file.name, "Cancelled by user")
                    return
                }

                val toRead = minOf(buffer.size.toLong(), contentLength - transferred).toInt()
                val read = input.read(buffer, 0, toRead)
                if (read == -1) break

                out.write(buffer, 0, read)
                transferred += read
                bytesSinceLastTime += read

                val now = System.currentTimeMillis()
                if (now - lastTime >= 500) {
                    val speed = (bytesSinceLastTime * 1000) / (now - lastTime)
                    onTransferProgress(file.name, startByte + transferred, totalSize, speed, TransferDirection.BACKUP_TO_LAPTOP)
                    lastTime = now
                    bytesSinceLastTime = 0L
                }
            }
            out.flush()
        }

        onTransferCompleted(file.name, totalSize, file.sha256, TransferDirection.BACKUP_TO_LAPTOP)
    }

    private fun handleBackupBundle(type: String, out: OutputStream) {
        val filesToBackup = fileProvider().filter {
            if (type == "incremental") {
                it.syncStatus != com.example.data.model.SyncStatus.SYNCED
            } else true
        }

        val responseHeaders = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/zip\r\n" +
                "Content-Disposition: attachment; filename=\"airbridge_backup_${System.currentTimeMillis()}.zip\"\r\n" +
                "Connection: close\r\n\r\n"
        out.write(responseHeaders.toByteArray())

        val zipOut = ZipOutputStream(out)
        val buffer = ByteArray(64 * 1024)

        for (file in filesToBackup) {
            val input: InputStream? = try {
                if (file.uriString.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(file.uriString))
                } else {
                    File(file.path).takeIf { it.exists() }?.let { FileInputStream(it) }
                }
            } catch (e: Exception) {
                null
            }

            if (input != null) {
                input.use { inStream ->
                    zipOut.putNextEntry(ZipEntry(file.name))
                    var read: Int
                    while (inStream.read(buffer).also { read = it } != -1) {
                        zipOut.write(buffer, 0, read)
                    }
                    zipOut.closeEntry()
                }
            }
        }
        zipOut.finish()
        out.flush()
    }

    private fun handleUpload(input: InputStream, headers: Map<String, String>, out: OutputStream) {
        val contentType = headers["content-type"] ?: ""
        val boundary = contentType.substringAfter("boundary=").trim()

        val downloadDir = context.getExternalFilesDir(null) ?: context.filesDir
        val targetFile = File(downloadDir, "received_${System.currentTimeMillis()}.dat")
        val fileOut = FileOutputStream(targetFile)

        val buffer = ByteArray(64 * 1024)
        var totalRead = 0L
        var read: Int
        fileOut.use { fos ->
            while (input.read(buffer).also { read = it } != -1) {
                fos.write(buffer, 0, read)
                totalRead += read
            }
        }

        val sha256 = try {
            FileInputStream(targetFile).use { CryptoUtils.computeSha256(it) }
        } catch (e: Exception) {
            ""
        }

        onTransferCompleted(targetFile.name, totalRead, sha256, TransferDirection.RESTORE_FROM_LAPTOP)

        val resp = JSONObject().apply {
            put("success", true)
            put("fileName", targetFile.name)
            put("bytesReceived", totalRead)
            put("sha256", sha256)
        }
        sendJsonResponse(out, resp.toString())
    }

    private fun sendJsonResponse(out: OutputStream, json: String, statusCode: Int = 200) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val statusText = if (statusCode == 200) "OK" else "Error"
        val response = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(response.toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun sendUnauthorized(out: OutputStream) {
        val body = "{\"error\": \"Unauthorized - Please authenticate with PIN\"}"
        sendJsonResponse(out, body, 401)
    }

    private fun sendNotFound(out: OutputStream) {
        val body = "{\"error\": \"Not Found\"}"
        sendJsonResponse(out, body, 404)
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (query.isBlank()) return map
        for (pair in query.split("&")) {
            val parts = pair.split("=")
            if (parts.isNotEmpty()) {
                val k = URLDecoder.decode(parts[0], "UTF-8")
                val v = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
                map[k] = v
            }
        }
        return map
    }
}
