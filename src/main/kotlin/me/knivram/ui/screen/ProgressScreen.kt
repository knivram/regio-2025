package me.knivram.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.*
import me.knivram.repository.Exercise
import me.knivram.repository.WorkoutRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

data class ProgressRecord(val date: String, val volume: Double)

class ProgressScreen(private val exercise: Exercise) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var records by remember { mutableStateOf(listOf<ProgressRecord>()) }
        var nextExpected by remember { mutableStateOf<Double?>(null) }

        LaunchedEffect(exercise) {
            val workouts = WorkoutRepository.getAll().filter { workout ->
                workout.exercises.any { it.exerciseId == exercise.id }
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val recs = workouts.map { workout ->
                val volume = workout.exercises
                    .filter { it.exerciseId == exercise.id }
                    .flatMap { it.sets }
                    .sumOf { it.weight * it.reps }
                ProgressRecord(workout.startDatetime.format(formatter), volume)
            }.sortedBy { it.date }

            if (recs.size >= 2) {
                val increases = recs.zipWithNext().map { (a, b) -> b.volume - a.volume }
                val avgIncrease = increases.average()
                nextExpected = recs.last().volume + avgIncrease
            }

            records = recs
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Progress of ${exercise.name}") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (records.isEmpty()) {
                    Text("No progress records available.")
                } else {
                    LazyColumn {
                        items(records) { record ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.date)
                                Text("Volume: ${record.volume}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    nextExpected?.let {
                        Text("Next Expected Volume: $it")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    VolumeLineChart(records, nextExpected)
                }
            }
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun VolumeLineChart(
    allRecords: List<ProgressRecord>,
    nextExpected: Double?
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val now = LocalDate.now()
    val cutoff = now.minusMonths(3)
    val filtered = allRecords.filter {
        val d = LocalDate.parse(it.date, dateFormatter)
        !d.isBefore(cutoff)
    }

    if (filtered.isEmpty()) {
        Text("No records in the last 3 months.")
        return
    }

    val categories = filtered.map { it.date }.toMutableList()

    val volumes = filtered.map { it.volume }.toMutableList()
    if (nextExpected != null) {
        categories.add("Expected")
        volumes.add(nextExpected)
    }

    val dataPoints = categories.zip(volumes) { dateString, vol ->
        DefaultPoint(dateString, vol.toFloat())
    }

    val maxVolume = (dataPoints.maxOfOrNull { it.y } ?: 0f)
    val yAxisMax = ceil(maxVolume / 50f) * 50f

    ChartLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        title = { ChartTitle("Total Volume") },
        legend = {},
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYGraph(
            xAxisModel = CategoryAxisModel(categories),
            yAxisModel = FloatLinearAxisModel(
                range = 0f..yAxisMax,
                minimumMajorTickSpacing = 50.dp
            ),
            xAxisLabels = { AxisLabel(it, Modifier.padding(top = 2.dp)) },
            xAxisTitle = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AxisTitle("Date")
                }
            },
            yAxisLabels = { value ->
                AxisLabel(value.toString(), Modifier.padding(end = 2.dp))
            },
            yAxisTitle = {
                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    AxisTitle(
                        "Volume in KG",
                        modifier = Modifier
                            .rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        ) {
            chartLine(dataPoints)
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun XYGraphScope<String, Float>.chartLine(
    data: List<DefaultPoint<String, Float>>
) {
    LinePlot(
        data = data,
        lineStyle = LineStyle(
            brush = SolidColor(Color(0xFF2196F3)),
            strokeWidth = 2.dp
        ),
        symbol = {
            Symbol(
                shape = CircleShape,
                fillBrush = SolidColor(Color(0xFF2196F3)),
                modifier = Modifier.size(6.dp)
            )
        }
    )
}


@Composable
fun ChartTitle(title: String) {
    Column {
        Text(
            title,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h4,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun AxisTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color = MaterialTheme.colors.onBackground,
        style = MaterialTheme.typography.body2,
        modifier = modifier
    )
}

@Composable
fun AxisLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        color = MaterialTheme.colors.onBackground,
        style = MaterialTheme.typography.body2,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
