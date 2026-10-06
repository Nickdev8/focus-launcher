package com.focus.launcher.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.focus.launcher.data.LaptopBatteryRepository
import com.focus.launcher.data.LaptopBatteryState
import com.focus.launcher.ui.components.T
import com.focus.launcher.ui.theme.LocalFocusColors
import com.focus.launcher.ui.theme.LocalTextScale
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
internal fun DeviceBatteryLines(phone: BatteryState, active: Boolean) {
    val colors = LocalFocusColors.current
    val owner = LocalLifecycleOwner.current
    val laptop by produceState<LaptopBatteryState?>(null, active, owner) {
        if (active) owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = null
            while (true) {
                value = LaptopBatteryRepository.fetch()
                delay(30_000)
            }
        }
    }
    val laptopText = when {
        laptop == null -> "unavailable"
        laptop?.connected != true -> "offline"
        laptop?.isFresh(Instant.now()) != true -> "stale"
        else -> batteryText(laptop?.percent, laptop?.charging)
    }
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        DeviceBatteryLine(
            batteryText(phone.percent.takeIf { it >= 0 }, phone.charging),
            laptop = false,
            color = if (phone.percent in 0..15 && !phone.charging) colors.fg else colors.faint,
        )
        DeviceBatteryLine(
            laptopText,
            laptop = true,
            color = if (laptop?.connected == true && laptop?.isFresh(Instant.now()) == true &&
                laptop?.percent in 0..15 && laptop?.charging == false) colors.fg else colors.faint,
        )
    }
}

private fun batteryText(percent: Int?, charging: Boolean?): String {
    if (percent == null) return "unavailable"
    val status = when (charging) {
        true -> "charging"
        false -> "not charging"
        null -> "status unknown"
    }
    return "$percent% · $status"
}

@Composable
private fun DeviceBatteryLine(text: String, laptop: Boolean, color: Color) {
    val scale = LocalTextScale.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Canvas(Modifier.size((12 * scale).dp).semantics {
            contentDescription = if (laptop) "Laptop battery" else "Phone battery"
        }) {
            val stroke = Stroke(width = 1.dp.toPx())
            if (laptop) {
                drawRect(color, Offset(size.width * .12f, size.height * .15f),
                    Size(size.width * .76f, size.height * .6f), style = stroke)
                drawLine(color, Offset(0f, size.height * .9f),
                    Offset(size.width, size.height * .9f), strokeWidth = stroke.width)
            } else {
                drawRoundRect(color, Offset(size.width * .25f, size.height * .05f),
                    Size(size.width * .5f, size.height * .9f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()), style = stroke)
                drawLine(color, Offset(size.width * .42f, size.height * .8f),
                    Offset(size.width * .58f, size.height * .8f), strokeWidth = stroke.width)
            }
        }
        T(text, size = 12.sp, color = color, maxLines = 1)
    }
}
