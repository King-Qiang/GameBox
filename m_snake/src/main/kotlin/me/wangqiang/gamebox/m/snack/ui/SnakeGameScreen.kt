package me.wangqiang.gamebox.m.snack.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun SnakeGameScreen(
    snake: List<Point>,
    food: Point,
    gridSize: Int,
    score: Int,
    gameState: GameState,
    modifier: Modifier = Modifier,
    onDirectionChange: (Direction) -> Unit = {},
    onRestart: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStep: () -> Unit = {}, // 新增：每帧调用的移动逻辑
    targetFps: Int = 60      // 新增：帧率可配置
) {
    // 游戏主循环协程
    LaunchedEffect(gameState == GameState.Running, score, targetFps) {
        if (gameState != GameState.Running) return@LaunchedEffect
        var lastFrameTime = 0L
        while (isActive && gameState == GameState.Running) {
            withFrameNanos { frameTime ->
                if (lastFrameTime == 0L) lastFrameTime = frameTime
                val baseInterval = 180L // 初始速度更慢
                val speedUp = max(180L, baseInterval - score * 4) // 最低80ms，随分数提升减缓
                if ((frameTime - lastFrameTime) >= speedUp * 1_000_000) {
                    onStep()
                    lastFrameTime = frameTime
                }
            }
            yield()
        }
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    Box(modifier = modifier
        .fillMaxSize()
        .background(Color(0xFFFAFAFA))
        .pointerInput(snake, gameState) {
            this.awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent().changes.first().also { pointerInput ->
                        if (pointerInput.pressed) {
                            Log.i("m_snack", "SnakeGameScreen onclick")
                            if (gameState != GameState.Running) return@awaitPointerEventScope
                            if (snake.isEmpty()) return@awaitPointerEventScope
                            val head = snake.first()
                            val minSidePx = with(density) {
                                minOf(
                                    configuration.screenWidthDp,
                                    configuration.screenHeightDp
                                ).dp.toPx()
                            }
                            val cellSize = minSidePx / gridSize
                            val gridPixelSize = cellSize * gridSize
                            val offsetX =
                                (with(density) { configuration.screenWidthDp.dp.toPx() } - gridPixelSize) / 2f
                            val offsetY =
                                (with(density) { configuration.screenHeightDp.dp.toPx() } - gridPixelSize) / 2f
                            val headCenterX = offsetX + head.x * cellSize + cellSize / 2
                            val headCenterY = offsetY + head.y * cellSize + cellSize / 2
                            val offset = pointerInput.position
                            val dx = offset.x - headCenterX
                            val dy = offset.y - headCenterY
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                            val threshold = cellSize * 1.2f // 扩大判定范围
                            if (distance < threshold) return@awaitPointerEventScope // 距离太近不转向，避免误触
                            val direction = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 0) Direction.Right else Direction.Left
                            } else {
                                if (dy > 0) Direction.Down else Direction.Up
                            }
                            onDirectionChange(direction)
                        }
                    }
                }
            }

//            detectTapGestures { offset ->
//                Log.i("m_snack", "SnakeGameScreen onclick")
//                if (gameState != GameState.Running) return@detectTapGestures
//                if (snake.isEmpty()) return@detectTapGestures
//                val head = snake.first()
//                val minSidePx = with(density) {
//                    minOf(
//                        configuration.screenWidthDp,
//                        configuration.screenHeightDp
//                    ).dp.toPx()
//                }
//                Log.i("m_snack", "SnakeGameScreen: configuration.screenWidthDp=${configuration.screenWidthDp}, configuration.screenHeightDp=${configuration.screenHeightDp}, minSidePx=${minSidePx}")
//                val cellSize = minSidePx / gridSize
//                val gridPixelSize = cellSize * gridSize
//                val offsetX =
//                    (with(density) { configuration.screenWidthDp.dp.toPx() } - gridPixelSize) / 2f
//                val offsetY =
//                    (with(density) { configuration.screenHeightDp.dp.toPx() } - gridPixelSize) / 2f
//                Log.i("m_snack", "SnakeGameScreen: offsetX=${offsetX}, offsetY=${offsetY}")
//                val headCenterX = offsetX + head.x * cellSize + cellSize / 2
//                val headCenterY = offsetY + head.y * cellSize + cellSize / 2
//                val dx = offset.x - headCenterX
//                val dy = offset.y - headCenterY
//                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
////                val threshold = cellSize * 1.2f // 扩大判定范围
////                if (distance < threshold) return@detectTapGestures // 距离太近不转向，避免误触
//                Log.i("m_snack", "SnakeGameScreen: head.x=${head.x}, head.y=${head.y}")
//                Log.i("m_snack", "SnakeGameScreen: offset.x=${offset.x}, offset.y=${offset.y}, headCenterX=$headCenterX, headCenterY=$headCenterY")
//                Log.i("m_snack", "SnakeGameScreen: dx=$dx, dy=$dy")
//                val direction = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
//                    if (dx > 0) Direction.Right else Direction.Left
//                } else {
//                    if (dy > 0) Direction.Down else Direction.Up
//                }
//                onDirectionChange(direction)
//            }
        }
    ) {
        // 计算正方形格子尺寸和偏移量，保证整体居中
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val minSidePx = with(density) { minOf(configuration.screenWidthDp, configuration.screenHeightDp).dp.toPx() }
        val cellSize = minSidePx / gridSize
        val gridPixelSize = cellSize * gridSize
        val offsetX = (with(density) { configuration.screenWidthDp.dp.toPx() } - gridPixelSize) / 2f
        val offsetY = (with(density) { configuration.screenHeightDp.dp.toPx() } - gridPixelSize) / 2f
        // 网格背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..gridSize) {
                // 竖线
                drawLine(
                    color = Color.LightGray,
                    start = Offset(offsetX + i * cellSize, offsetY),
                    end = Offset(offsetX + i * cellSize, offsetY + gridPixelSize),
                    strokeWidth = 1.dp.toPx()
                )
                // 横线
                drawLine(
                    color = Color.LightGray,
                    start = Offset(offsetX, offsetY + i * cellSize),
                    end = Offset(offsetX + gridPixelSize, offsetY + i * cellSize),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        // 蛇身
        Canvas(modifier = Modifier.fillMaxSize()) {
            snake.forEachIndexed { idx, p ->
                val color = if (idx == 0) Color(0xFF388E3C) else Color(0xFF66BB6A)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(offsetX + p.x * cellSize, offsetY + p.y * cellSize),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(cellSize * 0.3f, cellSize * 0.3f)
                )
            }
        }
        // 食物
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Red,
                radius = cellSize * 0.4f,
                center = Offset(
                    offsetX + food.x * cellSize + cellSize / 2,
                    offsetY + food.y * cellSize + cellSize / 2
                )
            )
        }
        // 分数计数器
        Text(
            text = "分数：$score",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )
        // 游戏状态覆盖层
        if (gameState != GameState.Running) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (gameState) {
                            GameState.Paused -> "游戏暂停"
                            GameState.Ended -> "游戏结束"
                            else -> ""
                        },
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        if (gameState == GameState.Paused) {
                            Button(onClick = onResume) { Text("继续") }
                        }
                        if (gameState == GameState.Ended) {
                            Button(onClick = onRestart) { Text("重新开始") }
                        }
                    }
                }
            }
        }
    }
}
// 关键代码已更新，所有格子均为正方形，整体居中。