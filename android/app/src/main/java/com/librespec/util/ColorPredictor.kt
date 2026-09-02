package com.librespec.util

import androidx.compose.ui.graphics.Color
import com.librespec.proto.BiochemSchema.SpectralTelemetry
import kotlin.math.max
import kotlin.math.pow

data class ChannelInfo(
    val name: String,
    val wavelengthNm: Int,
    val isVisible: Boolean,
    val protoIndex: Int,
    val colorHex: String,
    val responsivityRe: Float
)

data class PredictedColorResult(
    val color: Color,
    val hexCode: String,
    val colorName: String,
    val dominantWavelengthNm: Int,
    val dominantChannelName: String,
    val r: Int,
    val g: Int,
    val b: Int
)

object ColorPredictor {

    // AS7343 14 Channels as per Datasheet DS001046
    // Responsivities (Re) in counts per (mW/m^2) at AGAIN 1024x, 27.8ms
    val AS7343_CHANNELS = listOf(
        ChannelInfo("F1", 405, true, 0, "#6A00D7", 5749f),
        ChannelInfo("F2", 425, true, 1, "#4B0082", 1756f),
        ChannelInfo("FZ", 450, true, 8, "#0000FF", 2169f),
        ChannelInfo("F3", 475, true, 2, "#0080FF", 770f),
        ChannelInfo("F4", 515, true, 3, "#00FF80", 3141f),
        ChannelInfo("F5", 550, true, 4, "#80FF00", 1574f),
        ChannelInfo("FY", 555, true, 9, "#BFFF00", 3747f),
        ChannelInfo("FX", 600, true, 10, "#FF8000", 4776f),
        ChannelInfo("F6", 640, true, 5, "#FF0000", 3336f),
        ChannelInfo("F7", 690, true, 6, "#CC0000", 5435f),
        ChannelInfo("F8", 745, false, 7, "#800000", 864f),
        ChannelInfo("NIR", 855, false, 11, "#4A0000", 10581f),
        ChannelInfo("Clear", 0, false, 12, "#FFFFFF", 999f),
        ChannelInfo("FD", 0, false, 13, "#CCCCCC", 4311f)
    )

    // Sorted by wavelength for continuous spectral graph display
    val SORTED_SPECTRAL_CHANNELS = AS7343_CHANNELS
        .filter { it.wavelengthNm > 0 }
        .sortedBy { it.wavelengthNm }

    fun predictColor(telemetry: SpectralTelemetry?): PredictedColorResult {
        if (telemetry == null || telemetry.spectralChannelsCount < 14) {
            return PredictedColorResult(
                color = Color(0xFF333333),
                hexCode = "#333333",
                colorName = "Awaiting Data",
                dominantWavelengthNm = 0,
                dominantChannelName = "N/A",
                r = 51, g = 51, b = 51
            )
        }

        val rawChannels = telemetry.spectralChannelsList

        // Extract channel counts safely
        val cF1 = max(0f, rawChannels.getOrElse(0) { 0 }.toFloat() - 5f)
        val cF2 = max(0f, rawChannels.getOrElse(1) { 0 }.toFloat() - 5f)
        val cF3 = max(0f, rawChannels.getOrElse(2) { 0 }.toFloat() - 5f)
        val cF4 = max(0f, rawChannels.getOrElse(3) { 0 }.toFloat() - 5f)
        val cF5 = max(0f, rawChannels.getOrElse(4) { 0 }.toFloat() - 5f)
        val cF6 = max(0f, rawChannels.getOrElse(5) { 0 }.toFloat() - 5f)
        val cF7 = max(0f, rawChannels.getOrElse(6) { 0 }.toFloat() - 5f)
        val cFZ = max(0f, rawChannels.getOrElse(8) { 0 }.toFloat() - 5f)
        val cFY = max(0f, rawChannels.getOrElse(9) { 0 }.toFloat() - 5f)
        val cFX = max(0f, rawChannels.getOrElse(10) { 0 }.toFloat() - 5f)

        // Calculate normalized optical irradiance E_i = Count_i / Re_i
        val eF1 = cF1 / 5749f
        val eF2 = cF2 / 1756f
        val eFZ = cFZ / 2169f
        val eF3 = cF3 / 770f
        val eF4 = cF4 / 3141f
        val eF5 = cF5 / 1574f
        val eFY = cFY / 3747f
        val eFX = cFX / 4776f
        val eF6 = cF6 / 3336f
        val eF7 = cF7 / 5435f

        // Find dominant channel based on normalized optical irradiance E_i
        val visibleIrradiances = listOf(
            AS7343_CHANNELS[0] to eF1,
            AS7343_CHANNELS[1] to eF2,
            AS7343_CHANNELS[2] to eFZ,
            AS7343_CHANNELS[3] to eF3,
            AS7343_CHANNELS[4] to eF4,
            AS7343_CHANNELS[5] to eF5,
            AS7343_CHANNELS[6] to eFY,
            AS7343_CHANNELS[7] to eFX,
            AS7343_CHANNELS[8] to eF6,
            AS7343_CHANNELS[9] to eF7
        )

        val dominantPair = visibleIrradiances.maxByOrNull { it.second }
        val dominantChannel = dominantPair?.first ?: AS7343_CHANNELS[0]

        // CIE 1931 RGB Tristimulus calculation from normalized irradiances
        val rawR = 0.5f * eFX + 1.0f * eF6 + 0.4f * eF7 + 0.2f * eF5
        val rawG = 1.0f * eFY + 0.8f * eF4 + 0.7f * eF5 + 0.2f * eF3
        val rawB = 1.0f * eFZ + 0.7f * eF3 + 0.8f * eF2 + 0.5f * eF1

        val maxVal = max(rawR, max(rawG, rawB))

        if (maxVal <= 0.0001f) {
            return PredictedColorResult(
                color = Color(0xFF222222),
                hexCode = "#222222",
                colorName = "Dark / No Signal",
                dominantWavelengthNm = 0,
                dominantChannelName = "None",
                r = 34, g = 34, b = 34
            )
        }

        // Normalize RGB components
        var normR = (rawR / maxVal).coerceIn(0f, 1f)
        var normG = (rawG / maxVal).coerceIn(0f, 1f)
        var normB = (rawB / maxVal).coerceIn(0f, 1f)

        val gamma = 0.85f
        normR = normR.pow(gamma)
        normG = normG.pow(gamma)
        normB = normB.pow(gamma)

        val rInt = (normR * 255).toInt()
        val gInt = (normG * 255).toInt()
        val bInt = (normB * 255).toInt()

        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(rInt, gInt, bInt, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val valHue = hsv[2]

        val name = when {
            valHue < 0.1f -> "Dark"
            sat < 0.15f -> "White / Daylight"
            hue in 0f..18f || hue >= 345f -> "Red"
            hue in 18f..45f -> "Orange / Amber"
            hue in 45f..70f -> "Yellow"
            hue in 70f..165f -> "Green"
            hue in 165f..195f -> "Cyan"
            hue in 195f..255f -> "Blue"
            hue in 255f..285f -> "Purple / Violet"
            hue in 285f..345f -> "Magenta / Pink"
            else -> "Unknown"
        }

        val hexCode = String.format("#%02X%02X%02X", rInt, gInt, bInt)

        return PredictedColorResult(
            color = Color(android.graphics.Color.rgb(rInt, gInt, bInt)),
            hexCode = hexCode,
            colorName = name,
            dominantWavelengthNm = dominantChannel.wavelengthNm,
            dominantChannelName = "${dominantChannel.name} (${dominantChannel.wavelengthNm} nm)",
            r = rInt,
            g = gInt,
            b = bInt
        )
    }
}
