package com.focus.launcher.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Schema 2 made the split clock the default. Settings are stored with every field written out, so
 * a stored "RING" from before is the old default rather than a choice, and moves along once.
 * Everything a user did choose has to survive.
 */
class SettingsMigrationTest {

    @Test fun `legacy black and white choices survive theme migration`() {
        for (dark in listOf(true, false)) {
            val old = Settings(favorites = listOf("example/app"), textScale = 1.2f).toJson()
                .apply { remove("theme"); put("dark", dark) }
            val migrated = Settings.fromJson(old)
            assertEquals(if (dark) ThemeChoice.BLACK else ThemeChoice.WHITE, migrated.theme)
            assertEquals(listOf("example/app"), migrated.favorites)
            assertEquals(1.2f, migrated.textScale)
        }
    }

    @Test fun `theme choices persist and explicit choice takes precedence over legacy dark`() {
        for (theme in ThemeChoice.entries) {
            val chosen = Settings(theme = theme)
            assertEquals(chosen, Settings.fromJson(chosen.toJson().put("dark", false)))
        }
        assertEquals(ThemeChoice.BLACK, Settings.fromJson(JSONObject()).theme)
        assertEquals(ThemeChoice.WHITE, Settings.fromJson(JSONObject().put("theme", "UNKNOWN").put("dark", false)).theme)
    }

    @Test fun `system theme follows both system states while explicit choices stay fixed`() {
        for (systemDark in listOf(true, false)) {
            assertEquals(systemDark, ThemeChoice.SYSTEM.isDark(systemDark))
            assertEquals(true, ThemeChoice.BLACK.isDark(systemDark))
            assertEquals(false, ThemeChoice.WHITE.isDark(systemDark))
        }
    }

    private fun stored(version: Int?, style: String?): JSONObject = Settings().toJson().apply {
        if (version == null) remove("v") else put("v", version)
        if (style == null) remove("clockStyle") else put("clockStyle", style)
    }

    @Test fun `a fresh install gets the split clock`() {
        assertEquals(ClockStyle.SPLIT, Settings().clockStyle)
        assertEquals(ClockStyle.SPLIT, Settings.fromJson(JSONObject()).clockStyle)
    }

    @Test fun `a ring stored before schema 2 becomes the split clock`() {
        assertEquals(ClockStyle.SPLIT, Settings.fromJson(stored(version = null, style = "RING")).clockStyle)
        assertEquals(ClockStyle.SPLIT, Settings.fromJson(stored(version = 1, style = "RING")).clockStyle)
    }

    @Test fun `a plain clock chosen before schema 2 is kept`() {
        assertEquals(ClockStyle.PLAIN, Settings.fromJson(stored(version = null, style = "PLAIN")).clockStyle)
    }

    @Test fun `a ring chosen after the change is kept`() {
        val chosen = Settings(clockStyle = ClockStyle.RING)
        assertEquals(ClockStyle.RING, Settings.fromJson(chosen.toJson()).clockStyle)
    }

    @Test fun `settings survive a round trip`() {
        val s = Settings(clockStyle = ClockStyle.SPLIT, splitSide = SplitSide.SCREEN_TIME, showCalendar = true, doubleTapLock = false, musicAutoHide = false)
        assertEquals(s, Settings.fromJson(s.toJson()))
    }

    @Test fun `an install from before the music section could hide gets the hiding`() {
        val old = Settings(showMusic = true).toJson().apply { remove("musicAutoHide") }
        assertEquals(true, Settings.fromJson(old).musicAutoHide)
    }

    @Test fun `an unknown stored value falls back to the default`() {
        assertEquals(SplitSide.CALENDAR, Settings.fromJson(stored(version = 2, style = "SPLIT").put("splitSide", "MAIL")).splitSide)
    }
}
