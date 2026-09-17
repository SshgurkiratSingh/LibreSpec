package com.librespec.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.librespec.ble.BleRepository
import com.librespec.util.ColorPredictor
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

enum class GraphMode {
    SPECTRUM, KINETICS
}

@Composable
fun GraphScreen(bleRepository: BleRepository) {
    val telemetry by bleRepository.telemetryFlow.collectAsState()
    val isAcquiring by bleRepository.isAcquiring.collectAsState()

    var selectedGraphMode by remember { mutableStateOf(GraphMode.SPECTRUM) }

    // Sliding window of telemetry values for kinetics trajectory over time M(t) (max 100 points @ 10Hz = 10s)
    val kineticsHistory = remember { mutableStateListOf<Pair<Float, Float>>() }

    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    // Update chart when telemetry changes or graph mode changes
    LaunchedEffect(telemetry, selectedGraphMode) {
        telemetry?.let { data ->
            val channelsList = data.spectralChannelsList
            if (channelsList.size >= 14) {
                if (selectedGraphMode == GraphMode.SPECTRUM) {
                    // Spectrum mode: Plot 12 spectral channels sorted by wavelength (405nm to 855nm)
                    val entries = ColorPredictor.SORTED_SPECTRAL_CHANNELS.mapIndexed { index, channel ->
                        val rawVal = (channelsList.getOrElse(channel.protoIndex) { 0 }).toFloat()
                        FloatEntry(x = index.toFloat(), y = rawVal)
                    }
                    chartEntryModelProducer.setEntries(listOf(entries))
                } else {
                    // Kinetics mode: Track total visible intensity over time M(t)
                    val timeSec: Float = data.timestampMs.toFloat() / 1000f
                    var sumIntensity = 0f
                    for (i in 0..10) { // sum visible channels
                        sumIntensity += (channelsList.getOrElse(i) { 0 }).toFloat()
                    }

                    kineticsHistory.add(Pair(timeSec, sumIntensity))
                    if (kineticsHistory.size > 100) {
                        kineticsHistory.removeAt(0)
                    }

                    val entries = kineticsHistory.mapIndexed { index, pair ->
                        FloatEntry(x = index.toFloat(), y = pair.second)
                    }
                    chartEntryModelProducer.setEntries(listOf(entries))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val matchResult by com.librespec.service.MatcherService.matchResultFlow.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LibreSpec AS7343",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (matchResult != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Match: ${matchResult!!.materialName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", matchResult!!.confidenceScore)}% [${matchResult!!.phase}]",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Graph Mode Selector Tabs
        TabRow(
            selectedTabIndex = selectedGraphMode.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedGraphMode == GraphMode.SPECTRUM,
                onClick = { selectedGraphMode = GraphMode.SPECTRUM },
                text = { Text("Spectrum λ (405-855nm)") }
            )
            Tab(
                selected = selectedGraphMode == GraphMode.KINETICS,
                onClick = { selectedGraphMode = GraphMode.KINETICS },
                text = { Text("Kinetics M(t)") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (telemetry == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Awaiting 10Hz BLE stream...")
                }
            }
        } else {
            // Chart Display
            Chart(
                chart = lineChart(),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedGraphMode == GraphMode.SPECTRUM) {
                    "X-Axis: Wavelength Channels (405nm - 855nm) | Y-Axis: ADC Counts"
                } else {
                    "X-Axis: Rolling Time Frames (10Hz) | Y-Axis: Total Intensity M(t)"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Seq: ${telemetry?.sequenceNumber} | Time: ${telemetry?.timestampMs} ms",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of 14 Channel Values with Color Badges
            val currentChannels = telemetry?.spectralChannelsList ?: emptyList()
            if (currentChannels.size >= 14) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    items(ColorPredictor.AS7343_CHANNELS.size) { index ->
                        val ch = ColorPredictor.AS7343_CHANNELS[index]
                        val rawValue = currentChannels.getOrElse(ch.protoIndex) { 0 }
                        val badgeColor = try {
                            Color(android.graphics.Color.parseColor(ch.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        Row(
                            modifier = Modifier
                                .padding(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${ch.name}: $rawValue",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start / Stop Toggle Button
        Button(
            onClick = {
                if (isAcquiring) {
                    bleRepository.sendStopAcquisitionIntent()
                } else {
                    bleRepository.sendStartAcquisitionIntent()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAcquiring) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = if (isAcquiring) "STOP ACQUISITION" else "START ACQUISITION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isAcquiring) {
                    MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            )
        }
    }
}
