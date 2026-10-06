package com.focus.launcher.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant

data class LaptopBatteryState(
    val percent: Int?,
    val charging: Boolean?,
    val connected: Boolean,
    val updatedAt: Instant?,
) {
    fun isFresh(now: Instant): Boolean {
        val timestamp = updatedAt ?: return false
        val age = Duration.between(timestamp, now)
        return age <= MAX_AGE && age >= MAX_FUTURE_SKEW
    }

    companion object {
        private val MAX_AGE = Duration.ofMinutes(10)
        private val MAX_FUTURE_SKEW = Duration.ofMinutes(-1)
    }
}

object LaptopBatteryRepository {
    private const val ENDPOINT = "https://api.nickesselman.nl/device-state"
    private const val TIMEOUT_MS = 6_000
    private const val MAX_BODY_BYTES = 64 * 1024

    suspend fun fetch(): LaptopBatteryState? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(ENDPOINT).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.useCaches = false
            if (connection.responseCode !in 200..299) return@withContext null
            val bytes = connection.inputStream.use(::readBounded) ?: return@withContext null
            parse(bytes.toString(Charsets.UTF_8))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** Parses the public device-state response without retaining or logging its payload. */
    fun parse(json: String): LaptopBatteryState? {
        return try {
            val root = JSONObject(json)
            val laptop = root.optJSONObject("laptop") ?: return null
            val connected = laptop.opt("connected") as? Boolean ?: return null
            val percent = laptop.opt("batteryPercent").asIntPercentOrNull()
            val charging = laptop.opt("charging") as? Boolean
            val updatedAt = (root.opt("updatedAt") as? String)?.let {
                try { Instant.parse(it) } catch (_: Exception) { null }
            }
            LaptopBatteryState(percent, charging, connected, updatedAt)
        } catch (_: Exception) {
            null
        }
    }

    private fun readBounded(input: java.io.InputStream): ByteArray? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return output.toByteArray()
            if (output.size() + read > MAX_BODY_BYTES) return null
            output.write(buffer, 0, read)
        }
    }

    private fun Any?.asIntPercentOrNull(): Int? {
        val number = this as? Number ?: return null
        val value = number.toDouble()
        if (!value.isFinite() || value % 1.0 != 0.0 || value !in 0.0..100.0) return null
        return value.toInt()
    }
}
