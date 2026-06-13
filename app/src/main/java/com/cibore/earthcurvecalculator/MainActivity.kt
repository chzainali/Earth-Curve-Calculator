package com.cibore.earthcurvecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibore.earthcurvecalculator.ui.theme.EarthCurveCalculatorTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sqrt

enum class Screen {
    Splash, Calculator
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EarthCurveCalculatorTheme {
                var currentScreen by remember { mutableStateOf(Screen.Splash) }

                Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen(onSplashFinished = { currentScreen = Screen.Calculator })
                        Screen.Calculator -> CalculatorScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            Text(
                text = "🌍",
                fontSize = 100.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Earth Curve\nCalculator",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {
    var showInfoDialog by remember { mutableStateOf(false) }
    var isMetric by remember { mutableStateOf(true) }
    var eyeHeight by remember { mutableStateOf("") }
    var targetDistance by remember { mutableStateOf("") }
    
    var horizonDistanceResult by remember { mutableStateOf<Double?>(null) }
    var hiddenHeightResult by remember { mutableStateOf<Double?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earth Curve Calculator", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButtonOption(
                                text = "Metric",
                                selected = isMetric,
                                onClick = { 
                                    isMetric = true
                                    horizonDistanceResult = null
                                    hiddenHeightResult = null
                                }
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            RadioButtonOption(
                                text = "Imperial",
                                selected = !isMetric,
                                onClick = { 
                                    isMetric = false
                                    horizonDistanceResult = null
                                    hiddenHeightResult = null
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val heightUnit = if (isMetric) "metres" else "feet"
                        val distanceUnit = if (isMetric) "km" else "miles"

                        OutlinedTextField(
                            value = eyeHeight,
                            onValueChange = { eyeHeight = it },
                            label = { Text("h0 = Eye height ($heightUnit)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = targetDistance,
                            onValueChange = { targetDistance = it },
                            label = { Text("d0 = Target distance ($distanceUnit)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                calculate(
                                    eyeHeightStr = eyeHeight,
                                    targetDistanceStr = targetDistance,
                                    isMetric = isMetric,
                                    onResult = { d1, h1 ->
                                        horizonDistanceResult = d1
                                        hiddenHeightResult = h1
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Calculate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (horizonDistanceResult != null) {
                    Text(
                        text = "Results",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, start = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            ResultRow(
                                label = "d1 = Horizon distance",
                                value = String.format(Locale.getDefault(), "%.6f", horizonDistanceResult),
                                unit = if (isMetric) "km" else "miles"
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            ResultRow(
                                label = "h1 = Target hidden height",
                                value = String.format(Locale.getDefault(), "%.4f", hiddenHeightResult),
                                unit = if (isMetric) "metres" else "feet"
                            )
                        }
                    }
                }
            }
        }

        if (showInfoDialog) {
            InfoDialog(onDismiss = { showInfoDialog = false })
        }
    }
}

@Composable
fun RadioButtonOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ResultRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(
            text = unit,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "About Earth Curve Calculator", 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ) 
        },
        text = {
            Column {
                Text(
                    "This app calculates how much a distant object is obscured by the earth's curvature, and makes the following assumptions:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("• The earth is a convex sphere of radius 6371 kilometres", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Light travels in straight lines", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

fun calculate(
    eyeHeightStr: String,
    targetDistanceStr: String,
    isMetric: Boolean,
    onResult: (Double, Double) -> Unit
) {
    val h0 = eyeHeightStr.toDoubleOrNull() ?: 0.0
    val d0 = targetDistanceStr.toDoubleOrNull() ?: 0.0

    if (h0 <= 0.0 || d0 <= 0.0) {
        onResult(0.0, 0.0)
        return
    }

    val earthRadius = if (isMetric) 6371.0 else 3959.0
    val h0Base = if (isMetric) h0 / 1000.0 else h0 / 5280.0 // convert to earthRadius units (km or miles)

    // d1 = sqrt(2 * R * h0 + h0^2)
    val d1 = sqrt(2 * earthRadius * h0Base + h0Base * h0Base)

    val h1Base = if (d0 > d1) {
        val dDiff = d0 - d1
        // h1 = sqrt(dDiff^2 + R^2) - R
        sqrt(dDiff * dDiff + earthRadius * earthRadius) - earthRadius
    } else {
        0.0
    }

    val h1 = if (isMetric) h1Base * 1000.0 else h1Base * 5280.0

    onResult(d1, h1)
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    EarthCurveCalculatorTheme {
        SplashScreen(onSplashFinished = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    EarthCurveCalculatorTheme {
        CalculatorScreen()
    }
}
