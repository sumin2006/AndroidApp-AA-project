package com.example.a917sumin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a917sumin.ui.theme._917SuminTheme
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _917SuminTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("인생이 쓰다", style = MaterialTheme.typography.headlineMedium)
        Image(
            painter = painterResource(id = R.drawable.img_2), // ensure this image exists
            contentDescription = "고양이 귀여워",
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
        )
        Button(onClick = {}){
            Text("그래서 뭐할 수 있는데")
        }
    }
}
