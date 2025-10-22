package com.example.app_10_bubble_game

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.example.app_10_bubble_game.ui.theme.BubbleGameTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

const val ACTION_REPLY = "com.example.app_10_bubble_game.ACTION_REPLY"

// MainActivity: 앱의 진입점. setContent를 통해 Compose UI를 설정합니다.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BubbleGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 최종 버전의 게임 화면을 표시합니다.
                    BubbleGameScreen()
                }
            }
        }
    }
}

// 데이터 클래스: 개별 버블의 상태를 저장합니다.
data class Bubble(
    val id: Int,
    var position: Offset,
    val radius: Float,
    val color: Color,
    val creationTime: Long = System.currentTimeMillis(),
    val velocityX: Float = 0f,
    val velocityY: Float = 0f
)

// 게임 상태를 관리하는 클래스
class GameState {
    var bubbles by mutableStateOf<List<Bubble>>(emptyList())
    var score by mutableStateOf(0)
    var isGameOver by mutableStateOf(false)
    var gameTime by mutableStateOf(30) // 30초 제한 시간
}

/**
 * Step 1: 기본 버블 게임
 * - Canvas에 버블을 그립니다.
 * - 버블은 무작위 위치와 색상으로 나타납니다.
 * - 사용자가 버블을 터치하면 사라지고 점수가 올라갑니다.
 * - 일정 시간이 지나면 버블이 자동으로 사라집니다.
 * - Canvas 컴포저블은 기본적으로 자동 클리어 기능을 가지고 있어서, 매 프레임마다 캔버스가 자동으로 지워집니다.
 */
@Composable
fun BubbleGameStep1(gameState: GameState = remember { GameState() }) {
    val coroutineScope = rememberCoroutineScope()

    // 게임 루프: 게임이 끝나지 않았다면 1초마다 새 버블을 생성합니다.
    // LaunchedEffect의 핵심 역할은 컴포지션 수명주기 관리
    // LaunchedEffect는 Compose 컴포저블의 수명주기에 연결된 코루틴 실행기
    // gameState.isGameOver 값이 false → true로 변경되면 코루틴 자동으로 취소됨.
    LaunchedEffect(gameState.isGameOver) {
        if (!gameState.isGameOver) {
            launch {
                while (true) {
                    delay(1000) // 1초마다 실행
                    // 3초가 지난 버블 제거
                    val currentTime = System.currentTimeMillis()
                    gameState.bubbles = gameState.bubbles.filter { // filter()는 원본 리스트를 변경하지 않고 새 리스트 생성
                        currentTime - it.creationTime < 3000
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // 터치된 버블을 찾아 제거하고 점수를 올립니다.
                        gameState.bubbles.reversed().forEach { bubble ->
                            val distance = sqrt(
                                (offset.x - bubble.position.x).pow(2) +
                                        (offset.y - bubble.position.y).pow(2)
                            )
                            if (distance <= bubble.radius) {
                                /* 실제 Kotlin 표준 라이브러리 구현
                                operator fun <T> Iterable<T>.minus(element: T): List<T> {
                                    return this.toMutableList().apply { remove(element) }
                                } */
                                gameState.bubbles = gameState.bubbles - bubble
                                gameState.score++
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            // 새 버블 생성 (랜덤)
            if (Random.nextFloat() < 0.1f && gameState.bubbles.size < 15) {
                val newBubble = Bubble(
                    id = Random.nextInt(),
                    position = Offset(
                        x = Random.nextFloat() * size.width,
                        y = Random.nextFloat() * size.height
                    ),
                    radius = Random.nextFloat() * 50 + 50,
                    color = Color(
                        red = Random.nextInt(256),
                        green = Random.nextInt(256),
                        blue = Random.nextInt(256),
                        alpha = 200
                    )
                )
                gameState.bubbles = gameState.bubbles + newBubble
            }

            // 모든 버블을 화면에 다시 그립니다.
            gameState.bubbles.forEach { bubble ->
                drawCircle(
                    color = bubble.color,
                    radius = bubble.radius,
                    center = bubble.position
                )
            }
        }

        // 점수 표시
        Text(
            text = "Score: ${gameState.score}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Step 1: 버블 게임")
@Composable
fun BubbleGameStep1Preview() {
    BubbleGameTheme {
        val initialBubbles = listOf(
            Bubble(1, Offset(100f, 200f), 80f, Color.Red),
            Bubble(2, Offset(300f, 400f), 60f, Color.Blue),
            Bubble(3, Offset(500f, 150f), 70f, Color.Green)
        )
        val gameStateWithBubbles = remember { GameState().apply { bubbles = initialBubbles } }
        BubbleGameStep1(gameState = gameStateWithBubbles)
    }
}

/**
 * Step 2: 다이얼로그 추가
 * - 게임에 시간 제한을 둡니다.
 * - 시간이 다 되면 게임이 종료되고, AlertDialog로 최종 점수를 보여줍니다.
 * - '다시 시작' 버튼으로 게임을 재시작할 수 있습니다.
 */
@Composable
fun BubbleGameStep2(gameState: GameState = remember { GameState() }) {

    // 게임 타이머
    LaunchedEffect(gameState.isGameOver) {
        if (!gameState.isGameOver) {
            while (gameState.gameTime > 0) {
                delay(1000)
                gameState.gameTime--
            }
            gameState.isGameOver = true
        }
    }

    // 기본 게임 UI
    BubbleGameStep1(gameState)

    // 게임 오버 시 다이얼로그 표시
    if (gameState.isGameOver) {
        AlertDialog(
            onDismissRequest = { /* 다이얼로그 밖을 터치해도 닫히지 않음 */ },
            title = { Text("게임 종료!") },
            text = { Text("최종 점수: ${gameState.score}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 게임 상태 초기화 및 재시작
                        gameState.score = 0
                        gameState.bubbles = emptyList()
                        gameState.gameTime = 30
                        gameState.isGameOver = false
                    }
                ) {
                    Text("다시 시작")
                }
            }
        )
    }

    // 남은 시간 표시
    Text(
        text = "Time: ${gameState.gameTime}",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 50.dp, start = 16.dp)
    )
}

@Preview(showBackground = true, name = "Step 2: 다이얼로그 추가")
@Composable
fun BubbleGameStep2Preview() {
    BubbleGameTheme {
        val initialBubbles = listOf(
            Bubble(1, Offset(100f, 200f), 80f, Color.Red),
            Bubble(2, Offset(300f, 400f), 60f, Color.Blue)
        )
        val gameStateForPreview = remember { 
            GameState().apply { 
                bubbles = initialBubbles
                isGameOver = true
                gameTime = 0
                score = 123 // 예시 점수
            }
        }
        BubbleGameStep2(gameState = gameStateForPreview)
    }
}



/**
 * Step 4: 진동 알림 추가
 * - VibratorManager를 사용하여 버블이 터질 때 짧은 진동을 발생시킵니다.
 * - AndroidManifest.xml에 <uses-permission android:name="android.permission.VIBRATE" /> 권한이 필요합니다.
 */
@Composable
fun BubbleGameStep4(gameState: GameState = remember { GameState() }) {
    val context = LocalContext.current
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 진동 효과 함수
    fun vibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // 기본 게임 UI (Step 2)
    BubbleGameStep2(gameState)
    
    // 버블 터치 시 진동 발생 로직 추가
    // (이 또한 Step 5에서 통합 구현합니다.)
    /*
    if (distance <= bubble.radius) {
        gameState.bubbles = gameState.bubbles - bubble
        gameState.score++
        playPopSound()
        vibrate() // <-- 진동 발생!
        return@detectTapGestures
    }
    */
}

@Preview(showBackground = true, name = "Step 4: 진동 알림 추가")
@Composable
fun BubbleGameStep4Preview() {
    BubbleGameTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Step 4: 진동 추가", style = MaterialTheme.typography.headlineSmall)
            Text("버블을 터뜨릴 때 진동이 울립니다.\n(미리보기에서는 실제 진동이 오지 않을 수 있습니다.)", modifier = Modifier.padding(16.dp))
            // 실제 게임에서는 BubbleGameStep5를 실행하여 확인
            BubbleGameStep4()
        }
    }
}


/**
 * Step 5: 알림 띄우기 (모든 기능 통합)
 * - 사용자가 제공한 알림 코드를 Compose 환경에 맞게 수정하여 통합합니다.
 * - 게임 종료 후 '알림 보내기' 버튼을 통해 알림을 보낼 수 있습니다.
 * - 알림 권한을 요청하는 로직을 포함합니다.
 */
@Composable
fun BubbleGameScreen() {
    val context = LocalContext.current
    val gameState = remember { GameState() }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // --- Sound & Vibration Setup ---
    val vibrator = remember {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    }
    
    fun playPopEffect() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    // --- Notification Setup ---
    val notificationLauncher = rememberLauncherForActivityResult(  // 퍼미션 요청
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 승인되면 알림 표시
            showNotification(context, gameState.score)
        } else {
            Log.w("BubbleGame", "Notification permission denied.")
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showNotification(context, gameState.score)
            }
        } else {
            // TIRAMISU 이전 버전에서는 권한 요청 없이 바로 알림을 표시할 수 있습니다.
            showNotification(context, gameState.score)
        }
    }

    // --- Game Logic & UI ---
    val coroutineScope = rememberCoroutineScope()

    // 각 launch 블록은 게임의 독립적인 기능을 담당하는 별도의 코루틴으로, 동시에 실행되면서도 각기 다른 주기와 목적을 가집니다:
    LaunchedEffect(key1 = gameState.isGameOver) {
        if (!gameState.isGameOver) {
            // 게임 타이머: 게임 시간 관리 (1초 단위 감소)
            launch {
                while (gameState.gameTime > 0 && !gameState.isGameOver) {
                    delay(1000)
                    gameState.gameTime--
                }
                if (!gameState.isGameOver) {
                    gameState.isGameOver = true
                }
            }

            // 버블 물리엔진: 버블 자동 제거 및 이동
            launch {
                while (!gameState.isGameOver) {
                    delay(16) // 약 60 FPS
                    val currentTime = System.currentTimeMillis()
                    gameState.bubbles = gameState.bubbles.filter { currentTime - it.creationTime < 3000 }.map { bubble ->
                        // 버블 위치 업데이트
                        var newX = bubble.position.x + bubble.velocityX
                        var newY = bubble.position.y + bubble.velocityY

                        // 화면 경계 처리 (튕기기)
                        var newVelocityX = bubble.velocityX
                        var newVelocityY = bubble.velocityY

                        if (newX - bubble.radius < 0 || newX + bubble.radius > canvasSize.width) {
                            newVelocityX *= -1
                            newX = if (newX - bubble.radius < 0) bubble.radius else canvasSize.width - bubble.radius
                        }
                        if (newY - bubble.radius < 0 || newY + bubble.radius > canvasSize.height) {
                            newVelocityY *= -1
                            newY = if (newY - bubble.radius < 0) bubble.radius else canvasSize.height - bubble.radius
                        }
                        bubble.copy(position = Offset(newX, newY), velocityX = newVelocityX, velocityY = newVelocityY)
                    }
                }
            }
            // 새 버블 랜덤 생성: 0.1초 주기
            launch {
                while (!gameState.isGameOver) {
                    delay(100) // 0.1초마다 새 버블 생성 시도
                    if (canvasSize != androidx.compose.ui.geometry.Size.Zero && Random.nextFloat() < 0.2f && gameState.bubbles.size < 15) {
                        gameState.bubbles = gameState.bubbles + Bubble(
                            id = Random.nextInt(),
                            position = Offset(
                                x = Random.nextFloat() * canvasSize.width,
                                y = Random.nextFloat() * canvasSize.height
                            ),
                            radius = Random.nextFloat() * 50 + 50,
                            color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 200),
                            velocityX = Random.nextFloat() * 10 - 5, // -5 ~ 5 사이의 랜덤 속도
                            velocityY = Random.nextFloat() * 10 - 5  // -5 ~ 5 사이의 랜덤 속도
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { newSize ->
                    canvasSize = newSize.toSize()
                }
                .pointerInput(gameState.isGameOver) {
                    if (!gameState.isGameOver) {
                        detectTapGestures { offset ->
                            coroutineScope.launch {
                                val tappedBubble = gameState.bubbles.lastOrNull { bubble ->
                                    val distance = sqrt((offset.x - bubble.position.x).pow(2) + (offset.y - bubble.position.y).pow(2))
                                    distance <= bubble.radius
                                }
                                if (tappedBubble != null) {
                                    gameState.bubbles = gameState.bubbles - tappedBubble
                                    gameState.score++
                                    playPopEffect()
                                }
                            }
                        }
                    }
                }
        ) {
            // 버블 그리기
            gameState.bubbles.forEach { bubble ->
                drawCircle(color = bubble.color, radius = bubble.radius, center = bubble.position)
            }
        }

        // UI 요소
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: ${gameState.score}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Time: ${gameState.gameTime}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (gameState.isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("게임 종료!") },
                text = { Text("최종 점수: ${gameState.score}") },
                confirmButton = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { requestNotificationPermission() }) {
                            Text("결과 알림 받기")
                        }
                        TextButton(
                            onClick = {
                                gameState.score = 0
                                gameState.bubbles = emptyList()
                                gameState.gameTime = 30
                                gameState.isGameOver = false
                            }
                        ) {
                            Text("다시 시작")
                        }
                        TextButton(
                            onClick = {
                                (context as? ComponentActivity)?.finish() // 현재 액티비티 종료
                            }
                        ) {
                            Text("게임 종료")
                        }
                    }
                }
            )
        }
    }
}

// showNotification: 알림을 생성하고 표시하는 함수
// 채널 (Notification Channel): 안드로이드 8.0(API 26+)부터 도입된 알림 분류 시스템으로, 사용자가 알림 유형별로 설정을 제어할 수 있게 합니다.
fun showNotification(context: Context, score: Int) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "bubble-game-channel"
    val channelName = "Bubble Game Channel"
    val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
        description = "버블 게임 관련 알림 채널입니다."
        setShowBadge(true)
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        setSound(uri, audioAttributes)
        enableVibration(true)
    }
    manager.createNotificationChannel(channel)
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, channelId) // 알림 빌드 시 채널 지정
        .setSmallIcon(android.R.drawable.ic_dialog_info) // 적절한 아이콘으로 변경 필요
        .setContentTitle("버블 게임 결과")
        .setContentText("최종 점수: ${score}점! 다시 플레이하시겠어요?")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent) // 알림 탭 시 실행할 Intent 설정
        .setAutoCancel(true)

    // RemoteInput (답장 기능) 설정
    val KEY_TEXT_REPLY = "key_text_reply"
    val replyLabel = "메시지 보내기"
    val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
        setLabel(replyLabel)
        build()
    }
    val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
        action = ACTION_REPLY
    }
    val replyPendingIntent = PendingIntent.getBroadcast(
        context, 30, replyIntent, PendingIntent.FLAG_MUTABLE
    )

    val replyAction: NotificationCompat.Action =
        NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "답장",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

    builder.addAction(replyAction)

    manager.notify(11, builder.build())
}


@Preview(showBackground = true, name = "Step 5: 모든 기능 통합")
@Composable
fun BubbleGameStep5Preview() {
    BubbleGameTheme {
        BubbleGameScreen()
    }
}

// 알림의 답장을 처리하는 BroadcastReceiver   --> 14장 내용임.
// BroadcastReceiver 는 안드로이드 시스템의 브로드캐스트 메시지(시스템 이벤트 또는 앱 간 통신) 를 수신하고 처리하는 컴포넌트입니다.
// 주로 시스템 이벤트(배터리 부족, 부팅 완료 등)나 앱 간 통신에 사용됩니다.
class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 사용자가 입력한 텍스트를 가져옵니다.
        val replyTxt = RemoteInput.getResultsFromIntent(intent)?.getCharSequence("key_text_reply")
        Log.d("BubbleGame-Reply", "Received Reply: $replyTxt")

        // 알림을 닫습니다.
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(11)

        // TODO: 답장 받은 내용을 처리하는 로직 추가 (예: 서버 전송, DB 저장 등)
        Toast.makeText(context, "답변이 서버에 저장되었습니다!", Toast.LENGTH_SHORT).show()
    }
}
