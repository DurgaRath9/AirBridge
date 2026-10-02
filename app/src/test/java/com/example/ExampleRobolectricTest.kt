package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoUtils
import com.example.data.model.TransferProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AirBridge", appName)
    }

    @Test
    fun testCryptoSha256AndAesGcm() {
        val testData = "AirBridge Offline Transfer Payload".toByteArray(Charsets.UTF_8)
        val hash = CryptoUtils.computeSha256(ByteArrayInputStream(testData))
        assertNotNull(hash)
        assertEquals(64, hash.length)

        // Test AES-GCM
        val keyHex = CryptoUtils.generateAesKey()
        val keyBytes = CryptoUtils.hexToBytes(keyHex)
        val (iv, encrypted) = CryptoUtils.encryptAesGcm(testData, keyBytes)
        val decrypted = CryptoUtils.decryptAesGcm(encrypted, iv, keyBytes)
        assertEquals("AirBridge Offline Transfer Payload", String(decrypted, Charsets.UTF_8))
    }

    @Test
    fun testPairingPinFormat() {
        val pin = CryptoUtils.generatePairingPin()
        assertEquals(6, pin.length)
        assertTrue(pin.all { it.isDigit() })
    }

    @Test
    fun testFormatBytesAndSpeed() {
        assertEquals("500 B", TransferProgress.formatBytes(500))
        assertEquals("1.0 KB", TransferProgress.formatBytes(1024))
        assertEquals("10.0 MB", TransferProgress.formatBytes(10 * 1024 * 1024))
        assertEquals("2.00 GB", TransferProgress.formatBytes(2L * 1024 * 1024 * 1024))

        assertEquals("15.0 MB/s", TransferProgress.formatBytesSpeed(15 * 1024 * 1024))
    }
}
