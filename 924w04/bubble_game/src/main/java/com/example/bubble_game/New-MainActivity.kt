package com.example.bubble_game

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubble_game.ui.theme.TaewonyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaewonyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BubbleGameScreen()
                }
            }
        }
    }
}

data class Bubble(
    val id: Int,
    val position: Offset,
    val radius: Float,
    val color: Color,
    val creationTime: Long = System.currentTimeMillis(),
    val velocityX: Float = Random.nextFloat() * 8 - 4,
    val velocityY: Float = Random.nextFloat() * 8 - 4
)

class GameState(initialBubbles: List<Bubble> = emptyList()) {
    var bubbles by mutableStateOf(initialBubbles)
    var score by mutableStateOf(0)
    var isGameOver by mutableStateOf(false)
    var timeLeft by mutableStateOf(60)
    var bubbleIdCounter by mutableStateOf(0)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGameScreen() {
    val gameState: GameState = remember { GameState() }

    LaunchedEffect(gameState.isGameOver) {
        while (isActive && !gameState.isGameOver && gameState.timeLeft > 0) {
            delay(1000L)
            gameState.timeLeft--
            if (gameState.timeLeft == 0) {
                gameState.isGameOver = true
            }
            val currentTime = System.currentTimeMillis()
            gameState.bubbles = gameState.bubbles.filter {
                currentTime - it.creationTime < 3000
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GameStatusRow(score = gameState.score, timeLeft = gameState.timeLeft)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val canvasWidthPx = with(density) { maxWidth.toPx() }
            val canvasHeightPx = with(density) { maxHeight.toPx() }

            var lastBubbleSpawnTime by remember { mutableStateOf(System.currentTimeMillis()) }

            LaunchedEffect(gameState.isGameOver) {
                while (isActive && !gameState.isGameOver) {
                    delay(16)

                    if (gameState.bubbles.isEmpty()) {
                        gameState.bubbles = List(3) {
                            Bubble(
                                id = gameState.bubbleIdCounter++,
                                position = Offset(
                                    x = Random.nextFloat() * maxWidth.value,
                                    y = Random.nextFloat() * maxHeight.value
                                ),
                                radius = Random.nextFloat() * 25 + 25,
                                color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 200)
                            )
                        }
                    }

                    if (System.currentTimeMillis() - lastBubbleSpawnTime > 1000 && gameState.bubbles.size < 15) {
                        lastBubbleSpawnTime = System.currentTimeMillis()
                        val newBubble = Bubble(
                            id = gameState.bubbleIdCounter++,
                            position = Offset(
                                x = Random.nextFloat() * maxWidth.value,
                                y = Random.nextFloat() * maxHeight.value
                            ),
                            radius = Random.nextFloat() * 50 + 50,
                            color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 200)
                        )
                        gameState.bubbles = gameState.bubbles + newBubble
                    }

                    gameState.bubbles = gameState.bubbles.map { bubble ->
                        with(density) {
                            val radiusPx = bubble.radius.dp.toPx()
                            var xPx = bubble.position.x.dp.toPx()
                            var yPx = bubble.position.y.dp.toPx()
                            val vxPx = bubble.velocityX.dp.toPx()
                            val vyPx = bubble.velocityY.dp.toPx()

                            xPx += vxPx
                            yPx += vyPx

                            var newVx = bubble.velocityX
                            var newVy = bubble.velocityY

                            if (xPx < radiusPx || xPx > canvasWidthPx - radiusPx) newVx *= -1
                            if (yPx < radiusPx || yPx > canvasHeightPx - radiusPx) newVy *= -1

                            xPx = xPx.coerceIn(radiusPx, canvasWidthPx - radiusPx)
                            yPx = yPx.coerceIn(radiusPx, canvasHeightPx - radiusPx)

                            bubble.copy(
                                position = Offset(xPx.toDp().value, yPx.toDp().value),
                                velocityX = newVx,
                                velocityY = newVy
                            )
                        }
                    }
                }
            }

            gameState.bubbles.forEach { bubble ->
                BubbleComposable(bubble = bubble) {
                    gameState.score++
                    gameState.bubbles = gameState.bubbles.filterNot { it.id == bubble.id }
                }
            }

            if (gameState.isGameOver) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game Over!",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun BubbleComposable(bubble: Bubble, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .offset(x = bubble.position.x.dp, y = bubble.position.y.dp)
            .size((bubble.radius * 2).dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        drawCircle(
            color = bubble.color,
            radius = size.width / 2,
            center = center
        )
    }
}

@Composable
fun GameStatusRow(score: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Score: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Time: ${timeLeft}s", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleGamePreview() {
    TaewonyTheme {
        BubbleGameScreen()
    }
}