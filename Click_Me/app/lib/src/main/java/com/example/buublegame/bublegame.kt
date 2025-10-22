package com.example.bubblegame

// --- import 문 ---
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- 데이터 클래스 및 상태 관리 ---
data class Bubble(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val creationTime: Long = System.currentTimeMillis()
)

class GameState {
    var score by mutableStateOf(0)
    var timeLeft by mutableStateOf(30)
    var isGameOver by mutableStateOf(false)
    var bubbles by mutableStateOf(listOf<Bubble>())
    var speed by mutableStateOf(2f)
}

// --- 랜덤 색상 생성 ---
fun randomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}

// --- 버블 생성 함수 ---
fun makeNewBubble(maxWidth: Dp, maxHeight: Dp): Bubble {
    val id = Random.nextInt()
    val radius = Random.nextInt(30, 80).toFloat()
    val x = Random.nextFloat() * (maxWidth.value - radius)
    val y = Random.nextFloat() * (maxHeight.value - radius)
    return Bubble(id, x, y, radius, randomColor())
}

// --- 버블 위치 업데이트 함수 ---
fun updateBubblePositions(
    bubbles: List<Bubble>,
    canvasHeight: Float,
    speed: Float
): List<Bubble> {
    return bubbles.map { bubble ->
        val newY = bubble.y + speed
        val clampedY = if (newY + bubble.radius > canvasHeight) 0f else newY
        bubble.copy(y = clampedY)
    }
}

// --- 버블 UI ---
@Composable
fun BubbleComposable(bubble: Bubble, onClick: () -> Unit) {
    val offsetX = bubble.x.dp
    val offsetY = bubble.y.dp

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(bubble.radius.dp * 2)
            .background(bubble.color, shape = CircleShape)
            .clickable { onClick() }
    )
}

// --- 상단 점수/타이머 UI ---
@Composable
fun GameStatusRow(score: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "점수: $score", fontSize = 18.sp)
        Text(text = "남은 시간: $timeLeft초", fontSize = 18.sp)
    }
}

// --- 게임 전체 화면 ---
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGameScreen() {
    var gameState by remember { mutableStateOf(GameState()) }

    // 타이머 로직
    LaunchedEffect(gameState.timeLeft, gameState.isGameOver) {
        while (!gameState.isGameOver && gameState.timeLeft > 0) {
            delay(1000L)
            gameState.timeLeft--
            gameState.speed += 0.2f // 점점 빨라짐
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
            val canvasHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

            // 버블 물리 엔진
            LaunchedEffect(gameState.isGameOver) {
                while (!gameState.isGameOver) {
                    delay(16L)

                    if (gameState.bubbles.isEmpty()) {
                        gameState.bubbles = List(3) {
                            makeNewBubble(maxWidth, maxHeight)
                        }
                    }

                    if (Random.nextFloat() < 0.05f && gameState.bubbles.size < 15) {
                        val newBubble = makeNewBubble(maxWidth, maxHeight)
                        if (gameState.bubbles.none { it.id == newBubble.id }) {
                            gameState.bubbles += newBubble
                        }
                    }

                    gameState.bubbles = updateBubblePositions(
                        gameState.bubbles,
                        canvasHeightPx,
                        gameState.speed
                    )
                }
            }

            // 버블 UI
            gameState.bubbles.forEach { bubble ->
                BubbleComposable(bubble = bubble) {
                    gameState.score++
                    gameState.bubbles = gameState.bubbles.filterNot { it.id == bubble.id }
                }
            }

            // 게임 종료 메시지 + 다시 시작 버튼
            if (gameState.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "게임 종료!\n점수: ${gameState.score}",
                            color = Color.White,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { gameState = GameState() }) {
                            Text("다시 시작")
                        }
                    }
                }
            }
        }
    }
}