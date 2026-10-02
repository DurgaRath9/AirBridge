package com.example.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Self-contained, lightweight offline QR Code Generator.
 * Generates valid standard QR Code bit matrices and renders them to Bitmaps.
 */
object QrCodeGenerator {

    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val qrMatrix = encodeQr(content)
        val matrixSize = qrMatrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val scale = size.toFloat() / matrixSize

        for (y in 0 until size) {
            val my = (y / scale).toInt().coerceIn(0, matrixSize - 1)
            for (x in 0 until size) {
                val mx = (x / scale).toInt().coerceIn(0, matrixSize - 1)
                val isDark = qrMatrix[my][mx]
                bitmap.setPixel(x, y, if (isDark) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun generateQrImageBitmap(content: String, size: Int = 512): ImageBitmap {
        return generateQrBitmap(content, size).asImageBitmap()
    }

    private fun encodeQr(text: String): Array<BooleanArray> {
        // We generate a standard Version 3 (29x29) or Version 4 (33x33) QR matrix
        val dataBytes = text.toByteArray(Charsets.ISO_8859_1)
        val version = when {
            dataBytes.size <= 14 -> 1 // 21x21
            dataBytes.size <= 26 -> 2 // 25x25
            dataBytes.size <= 42 -> 3 // 29x29
            dataBytes.size <= 62 -> 4 // 33x33
            else -> 5 // 37x37
        }
        val dimension = 17 + 4 * version
        val matrix = Array(dimension) { BooleanArray(dimension) }
        val reserved = Array(dimension) { BooleanArray(dimension) }

        // Finder patterns
        addFinderPattern(matrix, reserved, 0, 0)
        addFinderPattern(matrix, reserved, dimension - 7, 0)
        addFinderPattern(matrix, reserved, 0, dimension - 7)

        // Alignment pattern for version >= 2
        if (version >= 2) {
            val alignPos = dimension - 7
            addAlignmentPattern(matrix, reserved, alignPos, alignPos)
        }

        // Timing patterns
        for (i in 8 until dimension - 8) {
            val bit = (i % 2 == 0)
            matrix[6][i] = bit
            reserved[6][i] = true
            matrix[i][6] = bit
            reserved[i][6] = true
        }

        // Dark module
        matrix[4 * version + 9][8] = true
        reserved[4 * version + 9][8] = true

        // Reserve format info areas
        for (i in 0..8) {
            reserved[8][i] = true
            reserved[i][8] = true
            reserved[8][dimension - 1 - i] = true
            reserved[dimension - 1 - i][8] = true
        }

        // Encode data bits into a bitstream
        val bits = mutableListOf<Boolean>()
        // Mode indicator: 8-bit byte mode = 0100
        bits.add(false); bits.add(true); bits.add(false); bits.add(false)
        // Character count (8 bits for version 1-9)
        for (i in 7 downTo 0) {
            bits.add(((dataBytes.size shr i) and 1) == 1)
        }
        // Data bytes
        for (b in dataBytes) {
            val value = b.toInt() and 0xFF
            for (i in 7 downTo 0) {
                bits.add(((value shr i) and 1) == 1)
            }
        }
        // Terminator
        repeat(4) { bits.add(false) }
        // Pad to byte boundary
        while (bits.size % 8 != 0) bits.add(false)
        // Pad codewords
        val pad1 = 0xEC
        val pad2 = 0x11
        var usePad1 = true
        val maxDataBits = getDataCapacity(version) * 8
        while (bits.size < maxDataBits) {
            val pad = if (usePad1) pad1 else pad2
            for (i in 7 downTo 0) {
                bits.add(((pad shr i) and 1) == 1)
            }
            usePad1 = !usePad1
        }

        // Error correction Reed-Solomon codewords
        val rawCodewords = mutableListOf<Int>()
        for (i in 0 until bits.size step 8) {
            var byteVal = 0
            for (j in 0 until 8) {
                if (i + j < bits.size && bits[i + j]) {
                    byteVal = byteVal or (1 shl (7 - j))
                }
            }
            rawCodewords.add(byteVal)
        }
        val ecCodewords = generateEcCodewords(rawCodewords, getEcCodewordsCount(version))
        val allCodewords = rawCodewords + ecCodewords

        // Convert all codewords to full bit list
        val allBits = mutableListOf<Boolean>()
        for (cw in allCodewords) {
            for (i in 7 downTo 0) {
                allBits.add(((cw shr i) and 1) == 1)
            }
        }

        // Place bits into matrix in zigzag order with mask pattern 0: (x + y) % 2 == 0
        var bitIndex = 0
        var goingUp = true
        var col = dimension - 1
        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing column
            val rowRange = if (goingUp) (dimension - 1 downTo 0) else (0 until dimension)
            for (row in rowRange) {
                for (c in 0..1) {
                    val x = col - c
                    val y = row
                    if (!reserved[y][x]) {
                        val bit = if (bitIndex < allBits.size) allBits[bitIndex++] else false
                        val mask = ((x + y) % 2 == 0)
                        matrix[y][x] = bit xor mask
                    }
                }
            }
            goingUp = !goingUp
            col -= 2
        }

        // Format info (Mask 0, Error Correction Level L: 01)
        val formatInfo = 0x77C4 // Precomputed BCH code for L level and mask 000
        for (i in 0..14) {
            val bit = ((formatInfo shr (14 - i)) and 1) == 1
            // Along top-left finder
            when {
                i < 6 -> matrix[8][i] = bit
                i == 6 -> matrix[8][7] = bit
                i == 7 -> matrix[8][8] = bit
                i == 8 -> matrix[7][8] = bit
                else -> matrix[14 - i][8] = bit
            }
            // Along bottom-left and top-right finders
            if (i < 8) {
                matrix[dimension - 1 - i][8] = bit
            } else {
                matrix[8][dimension - 15 + i] = bit
            }
        }

        return matrix
    }

    private fun addFinderPattern(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, startX: Int, startY: Int) {
        for (y in 0..6) {
            for (x in 0..6) {
                val isBlack = (x == 0 || x == 6 || y == 0 || y == 6 || (x in 2..4 && y in 2..4))
                matrix[startY + y][startX + x] = isBlack
                reserved[startY + y][startX + x] = true
            }
        }
        // Separator
        val dim = matrix.size
        for (y in -1..7) {
            for (x in -1..7) {
                val px = startX + x
                val py = startY + y
                if (px in 0 until dim && py in 0 until dim) {
                    reserved[py][px] = true
                }
            }
        }
    }

    private fun addAlignmentPattern(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, centerX: Int, centerY: Int) {
        for (y in -2..2) {
            for (x in -2..2) {
                val isBlack = (kotlin.math.abs(x) == 2 || kotlin.math.abs(y) == 2 || (x == 0 && y == 0))
                matrix[centerY + y][centerX + x] = isBlack
                reserved[centerY + y][centerX + x] = true
            }
        }
    }

    private fun getDataCapacity(version: Int): Int = when (version) {
        1 -> 19
        2 -> 34
        3 -> 55
        4 -> 80
        else -> 108
    }

    private fun getEcCodewordsCount(version: Int): Int = when (version) {
        1 -> 7
        2 -> 10
        3 -> 15
        4 -> 20
        else -> 26
    }

    private fun generateEcCodewords(data: List<Int>, ecCount: Int): List<Int> {
        // Galois Field GF(256) arithmetic for Reed-Solomon code
        val exp = IntArray(512)
        val log = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            log[x] = i
            x = (x shl 1) xor if (x >= 128) 0x11D else 0
        }
        for (i in 255 until 512) {
            exp[i] = exp[i - 255]
        }

        // Generator polynomial
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val root = exp[i]
            val nextGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                nextGen[j] = nextGen[j] xor gen[j]
                val prod = if (gen[j] == 0 || root == 0) 0 else exp[(log[gen[j]] + log[root]) % 255]
                nextGen[j + 1] = nextGen[j + 1] xor prod
            }
            gen = nextGen
        }

        val result = IntArray(ecCount)
        for (item in data) {
            val factor = item xor result[0]
            for (i in 0 until ecCount - 1) {
                val prod = if (factor == 0 || gen[i + 1] == 0) 0 else exp[(log[factor] + log[gen[i + 1]]) % 255]
                result[i] = result[i + 1] xor prod
            }
            val prod = if (factor == 0 || gen[ecCount] == 0) 0 else exp[(log[factor] + log[gen[ecCount]]) % 255]
            result[ecCount - 1] = prod
        }

        return result.toList()
    }
}
