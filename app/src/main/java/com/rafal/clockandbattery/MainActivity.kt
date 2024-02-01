package com.rafal.clockandbattery

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FullScreenClock()
        }
    }
}

private fun getBatteryInfo(context: Context): Pair<Int, Boolean> {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val chargingState = batteryManager.isCharging
    return Pair(batteryPercentage, chargingState)
}

private fun hasWriteSettingsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
    if (!Settings.System.canWrite(context)) {
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }
}

@Composable
private fun FullScreenClock() {
    var batteryLevel by remember { mutableIntStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val (level, charging) = getBatteryInfo(context)
    batteryLevel = level
    isCharging = charging

    hasWriteSettingsPermission(context)

    LaunchedEffect(key1 = Unit) {
        while (true) {
            if (batteryLevel in 21..79 && isCharging) {
                // Wyłącz ładowanie
                stopCharging(context)
            } else if (batteryLevel !in 21..79 && !isCharging) {
                // Włącz ładowanie
                startCharging(context)
            }
            val currentTime = Calendar.getInstance().time
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(currentTime)
            if (formattedTime >= "08:00" && formattedTime <= "22:00") {
                BrightnessManager.setBrightness(contentResolver, BrightnessManager.getMaxBrightness())
            } else {
                // Domyślna jasność ekranu
                BrightnessManager.setBrightness(contentResolver, 150)
            }
            delay(30 * 60 * 1000) // sprawdzaj co pół godziny
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val time = getTime()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = getMaxFontSizeForText(time)
            )
            Text(
                text = if (isCharging) "⚡⚡⚡ $batteryLevel $isCharging" else "\uD83D\uDD0B $batteryLevel $isCharging",
                fontSize = 20.sp
            )
        }
    }
}
private fun getTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

object BrightnessManager {
    fun setBrightness(contentResolver: ContentResolver, brightness: Int) {
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
    }
    fun getMaxBrightness(): Int {
        return 255 // maksymalna wartość jasności ekranu
    }
}

@Composable
fun getMaxFontSizeForText(text: String): TextUnit {
    val screenWidth = LocalDensity.current.run { 1000.dp.toPx() }
    val textSize = screenWidth / (text.length * 0.6f)
    val scaledTextSize = if (textSize > 200) 200f else textSize
    return scaledTextSize.sp
}

private fun startCharging(context: Context) {
//TODO
}

private fun stopCharging(context: Context) {
    //TODO
}
