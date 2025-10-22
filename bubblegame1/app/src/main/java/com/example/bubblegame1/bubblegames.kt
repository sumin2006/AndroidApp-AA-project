package com.example.bubblegame1

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import java.util.UUID

// --- 데이터 클래스 ---
data class Bubble(
    val id: String = UUID.randomUUID().toString(),
    val x: Dp,
    val y: Dp,
    val radius: Dp,
    val dx: Dp,
    val dy: Dp,
    val color: Color,
    val creationTime: Long = System.currentTimeMillis()
)

// --- 게임 상태 클래스 ---
class GameState {
    var score by mutableStateOf(0)
    var timeLeft by mutableStateOf(30)
    var isGameOver by mutableStateOf(false)
    var bubbles by mutableStateOf(listOf<Bubble>())
}

// --- 버블 생성 함수 ---
fun makeNewBubble(width: Dp, height: Dp, density: Density): Bubble {
    val radius = (40..100).random().dp
    val widthPx = with(density) { width.toPx() }
    val heightPx = with(density) { height.toPx() }
    val radiusPx = with(density) { radius.toPx() }

    val xPx = radiusPx + Random.nextFloat() * (widthPx - radiusPx * 2)
    val yPx = radiusPx + Random.nextFloat() * (heightPx - radiusPx * 2)

    val dxPx = (Random.nextFloat() - 0.5f) * with(density) { 8.dp.toPx() }
    val dyPx = (Random.nextFloat() - 0.5f) * with(density) { 8.dp.toPx() }

    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan, Color.Yellow)
    val color = colors.random()

    return Bubble(
        x = with(density) { xPx.toDp() },
        y = with(density) { yPx.toDp() },
        radius = radius,
        dx = with(density) { dxPx.toDp() },
        dy = with(density) { dyPx.toDp() },
        color = color
    )
}

// --- 버블 위치 업데이트 함수 ---
fun updateBubblePositions(
    bubbles: List<Bubble>,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    density: Density
): List<Bubble> {
    return bubbles.map { bubble ->
        val newX = bubble.x + bubble.dx
        val newY = bubble.y + bubble.dy

        val radiusPx = with(density) { bubble.radius.toPx() }
        val maxX = canvasWidthPx - radiusPx
        val maxY = canvasHeightPx - radiusPx

        val dx = if (with(density) { newX.toPx() } < 0 || with(density) { newX.toPx() } > maxX) -bubble.dx else bubble.dx
        val dy = if (with(density) { newY.toPx() } < 0 || with(density) { newY.toPx() } > maxY) -bubble.dy else bubble.dy

        bubble.copy(x = newX, y = newY, dx = dx, dy = dy)
    }
}

// --- 버블 UI 컴포저블 ---
@Composable
fun BubbleComposable(bubble: Bubble, onClick: () -> Unit) {
    var isPopped by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPopped) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isPopped) onClick()
        }
    )

    Box(
        modifier = Modifier
            .offset(bubble.x, bubble.y)
            .size(bubble.radius * 2)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = 0.6f) // 반투명
            .clickable { isPopped = true }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = bubble.color
        ) {}

        // 속도 및 위치 정보 표시
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "x:${bubble.x.value.toInt()}, y:${bubble.y.value.toInt()}",
                fontSize = 10.sp,
                color = Color.White
            )
            Text(
                text = "dx:${bubble.dx.value.toInt()}, dy:${bubble.dy.value.toInt()}",
                fontSize = 10.sp,
                color = Color.White
            )

        }
    }
}

// --- 게임 상태 표시 UI ---
@Composable
fun GameStatusRow(score: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("점수: $score", style = MaterialTheme.typography.titleMedium)
        Text("남은 시간: $timeLeft 초", style = MaterialTheme.typography.titleMedium)
    }
}

// --- 게임 전체 화면 ---
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGameScreen() {
    var isStarted by remember { mutableStateOf(false) }
    val gameState = remember { GameState() }

    if (!isStarted) {
        // 시작 화면
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("버블 게임", fontSize = 32.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { isStarted = true }) {
                    Text("게임 시작")
                }
            }
        }
        return
    }

    // 타이머 로직
    LaunchedEffect(Unit) {
        while (!gameState.isGameOver && gameState.timeLeft > 0) {
            delay(1000L)
            gameState.timeLeft--
            if (gameState.timeLeft == 0) {
                gameState.isGameOver = true
            }
            val currentTime = System.currentTimeMillis()
            gameState.bubbles = gameState.bubbles.filter {
                currentTime - it.creationTime < 8000
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GameStatusRow(score = gameState.score, timeLeft = gameState.timeLeft)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val canvasWidthPx = with(density) { maxWidth.toPx() }
            val canvasHeightPx = with(density) { maxHeight.toPx() }

            // 물리 엔진
            LaunchedEffect(Unit) {
                while (!gameState.isGameOver) {
                    delay(16)
                    if (gameState.bubbles.isEmpty()) {
                        val newBubbles = List(3) {
                            makeNewBubble(maxWidth, maxHeight, density)
                        }
                        gameState.bubbles = newBubbles
                    }

                    val spawnChance = 0.05f + (30 - gameState.timeLeft) * 0.002f
                    if (Random.nextFloat() < spawnChance && gameState.bubbles.size < 15) {
                        val newBubble = makeNewBubble(maxWidth, maxHeight, density)
                        gameState.bubbles = gameState.bubbles + newBubble
                    }

                    gameState.bubbles = updateBubblePositions(
                        gameState.bubbles,
                        canvasWidthPx,
                        canvasHeightPx,
                        density
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

            // 게임 종료 UI
            if (gameState.isGameOver) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "🎉 게임 종료 🎉\n 최종 점수: ${gameState.score}\n 수고했어요!",
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            gameState.score = 0
                            gameState.timeLeft = 30
                            gameState.isGameOver = false
                            gameState.bubbles = emptyList()
                            isStarted = false
                        }) {
                            Text("다시 시작")
                        }
                    }
                }
            }
        }
    }
}


// --- 테마 정의 ---
@Composable
fun SuminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography(),
        content = content
    )
}

// --- 프리뷰 ---
@Preview(showBackground = true)
@Composable
fun BubbleGamePreview() {
    SuminTheme {
        BubbleGameScreen()
    }
}
