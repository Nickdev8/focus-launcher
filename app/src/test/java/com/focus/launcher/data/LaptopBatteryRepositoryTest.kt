package com.focus.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LaptopBatteryRepositoryTest {
    @Test
    fun `parses laptop fields and ignores other device data`() {
        val state = LaptopBatteryRepository.parse(
            """{"updatedAt":"2026-10-06T10:00:00Z","laptop":{"connected":true,"batteryPercent":73,"charging":false},"phone":{"batteryPercent":1}}""",
        )

        assertEquals(LaptopBatteryState(73, false, true, Instant.parse("2026-10-06T10:00:00Z")), state)
    }

    @Test
    fun `missing optional and invalid battery fields become unknown`() {
        val state = LaptopBatteryRepository.parse(
            """{"laptop":{"connected":false,"batteryPercent":101,"charging":"unknown"}}""",
        )

        assertEquals(LaptopBatteryState(null, null, false, null), state)
    }

    @Test
    fun `missing or invalid required structure is rejected`() {
        assertNull(LaptopBatteryRepository.parse("not-json"))
        assertNull(LaptopBatteryRepository.parse("{}"))
        assertNull(LaptopBatteryRepository.parse("""{"laptop":{"connected":"yes"}}"""))
    }

    @Test
    fun `freshness rejects missing stale and too-far-future timestamps`() {
        val now = Instant.parse("2026-10-06T10:00:00Z")
        assertFalse(LaptopBatteryRepository.parse("""{"laptop":{"connected":true}}""")!!.isFresh(now))
        assertFalse(stateAt("2026-10-06T09:49:59Z").isFresh(now))
        assertFalse(stateAt("2026-10-06T10:01:01Z").isFresh(now))
        assertTrue(stateAt("2026-10-06T09:50:00Z").isFresh(now))
        assertTrue(stateAt("2026-10-06T10:01:00Z").isFresh(now))
    }

    private fun stateAt(timestamp: String) = LaptopBatteryRepository.parse(
        """{"updatedAt":"$timestamp","laptop":{"connected":true,"batteryPercent":50,"charging":true}}""",
    )!!
}
