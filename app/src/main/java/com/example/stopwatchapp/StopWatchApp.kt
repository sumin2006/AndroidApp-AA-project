package com.example.stopwatchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopwatchapp.ui.theme.StopWatchAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StopWatchAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val count = remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CounterApp(count)
        Spacer(modifier = Modifier.height(32.dp))
        StopWatchApp()
    }
}

@Composable
fun CounterApp(count: MutableState<Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Count: ${count.value}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { count.value++ }) {
                Text("Increase")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { count.value = 0 }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun StopWatchApp() {
    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isActive && isRunning) {
            delay(10L)
            time += 10L
        }
    }

    val seconds = (time / 1000).toInt()
    val minutes = seconds / 60
    val displaySeconds = seconds % 60
    val millis = (time % 1000) / 10

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d:%02d:%02d", minutes, displaySeconds, millis),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { isRunning = true }) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { isRunning = false }) {
                Text("Stop")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                isRunning = false
                time = 0L
            }) {
                Text("Reset")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    StopWatchAppTheme {
        AppContent()
    }
}

