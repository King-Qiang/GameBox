package me.wangqiang.gamebox.m.snack.activity

import androidx.activity.ComponentActivity
import com.alibaba.android.arouter.facade.annotation.Route
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_FlappyBird
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Route(path = RoutePath_FlappyBird.Page.FlappyBird_Main)
class FlappyBirdActivity:  ComponentActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            FlappyBirdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlappyBirdGame()
                }
            }
        }
    }
}

@Composable
fun FlappyBirdGame() {
    // 游戏状态
    var gameState by remember { mutableStateOf(GameState.NOT_STARTED) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    // 静态小草
    val grassBlades = remember {
        List(50) {
            GrassBlade(
                x = Random.nextFloat(),
                height = Random.nextFloat() * 0.8f,
                width = Random.nextFloat() * 0.2f
            )
        }
    }

    // 小鸟属性
    val birdY = remember { Animatable(0.5f) } // 0-1范围
    val birdVelocity = remember { mutableFloatStateOf(0f) }
    val birdRotation = remember { Animatable(0f) }

    // 管道属性
    val pipes = remember { mutableStateListOf<Pipe>() }
    val pipeScroll = remember { mutableFloatStateOf(0f) }

    // 游戏参数
    val gravity = 0.000001f
    val jumpForce = -0.0003f
    val pipeSpeed = 0.0002f
    val pipeGap = 0.25f // 管道间隙高度占比
    val pipeWidth = 0.1f // 管道宽度占比
    val birdSize = 0.04f // 小鸟大小

    // 游戏循环控制
    val coroutineScope = rememberCoroutineScope()
    var frameTime by remember { mutableLongStateOf(0L) }

    // 游戏区域尺寸
    var gameSize by remember { mutableStateOf(Size.Zero) }

    // 开始游戏
    fun startGame() {
        gameState = GameState.RUNNING
        score = 0
        coroutineScope.launch {
            birdY.snapTo(0.5f)
            birdRotation.snapTo(0f)
        }
        birdVelocity.value = 0f
        pipes.clear()
        pipeScroll.value = 0f
        frameTime = System.currentTimeMillis()
    }

    // 游戏结束
    fun endGame() {
        gameState = GameState.GAME_OVER
        if (score > bestScore) {
            bestScore = score
        }
    }

    // 小鸟跳跃
    fun jump() {
        if (gameState == GameState.RUNNING) {
            birdVelocity.value = jumpForce
            coroutineScope.launch {
                birdRotation.animateTo(-30f, animationSpec = tween(100))
            }
        } else if (gameState == GameState.NOT_STARTED) {
            startGame()
        }
    }

    // 游戏主循环
    LaunchedEffect(gameState) {
        while (true) {
            if (gameState == GameState.RUNNING) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - frameTime).coerceAtMost(50) // 限制最大50ms
                frameTime = currentTime

                // 更新小鸟位置
                birdVelocity.value += gravity * deltaTime
                val newY = birdY.value + birdVelocity.value * deltaTime
                if (newY < birdSize/2 || newY > 1 - birdSize/2) {
                    endGame()
                } else {
                    birdY.snapTo(newY)
                }

                // 更新小鸟旋转
                if (birdVelocity.value > 0) {
                    birdRotation.snapTo(minOf(birdRotation.value + 1f, 90f))
                }

                // 更新管道位置
                pipeScroll.value += pipeSpeed * deltaTime
                if (pipeScroll.value > 0.35f) {
                    pipeScroll.value = 0f
                    // 添加新管道
                    val gapPosition = Random.nextFloat() * (1 - pipeGap - 0.1f) + 0.05f
                    pipes.add(
                        Pipe(
                            position = 1f,
                            gapPosition = gapPosition,
                            gapSize = pipeGap,
                            passed = false
                        )
                    )
                }

                // 更新管道位置并检测碰撞
                val pipesToRemove = mutableListOf<Pipe>()
                for (pipe in pipes) {
                    val newPosition = pipe.position - pipeSpeed * deltaTime

                    // 检测是否通过管道
                    if (!pipe.passed && newPosition < 0.3f) {
                        pipe.passed = true
                        score++
                    }

                    // 移除屏幕外的管道
                    if (newPosition < -pipeWidth) {
                        pipesToRemove.add(pipe)
                    } else {
                        pipes[pipes.indexOf(pipe)] = pipe.copy(position = newPosition)
                    }

                    // 碰撞检测 - 只在管道在屏幕右侧时检测
                    if (pipe.position < 0.5f &&
                        checkCollision(
                            birdY = birdY.value,
                            birdSize = birdSize,
                            pipe = pipe,
                            pipeWidth = pipeWidth
                        )
                    ) {
                        endGame()
                    }
                }
                pipes.removeAll(pipesToRemove)
            }
            delay(16) // 约60fps
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF70C5CE))
            .pointerInput(Unit) {
                this.awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.first().also { pointerInput ->
                            if (pointerInput.pressed) {
                                jump()
                            }
                        }
                    }
                }
            }
    ) {
        // 游戏画布
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            gameSize = size

            // 绘制背景
            drawRect(color = Color(0xFF70C5CE))

            // 绘制云朵
            for (i in 0..3) {
                val x = (size.width * (i * 0.3f + pipeScroll.floatValue * 0.5f)) % (size.width * 1.5f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = Offset(x, size.height * 0.2f),
                    radius = size.width * 0.05f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = Offset(x + size.width * 0.03f, size.height * 0.18f),
                    radius = size.width * 0.04f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = Offset(x - size.width * 0.03f, size.height * 0.18f),
                    radius = size.width * 0.04f
                )
            }

            // 绘制管道
            // 绘制管道
            for (pipe in pipes) {
                // 上管道
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF5EBC6B), Color(0xFF2E7D32)),
                        startY = 0f,
                        endY = pipe.gapPosition * size.height
                    ),
                    topLeft = Offset(
                        x = pipe.position * size.width,
                        y = 0f
                    ),
                    size = Size(
                        width = pipeWidth * size.width,
                        height = pipe.gapPosition * size.height
                    ),
                    cornerRadius = CornerRadius(size.width * 0.02f)
                )

                // 下管道
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF5EBC6B), Color(0xFF2E7D32)),
                        startY = (pipe.gapPosition + pipe.gapSize) * size.height,
                        endY = size.height
                    ),
                    topLeft = Offset(
                        x = pipe.position * size.width,
                        y = (pipe.gapPosition + pipe.gapSize) * size.height
                    ),
                    size = Size(
                        width = pipeWidth * size.width,
                        height = size.height * (1 - pipe.gapPosition - pipe.gapSize)
                    ),
                    cornerRadius = CornerRadius(size.width * 0.02f)
                )

                // 管道装饰条纹 (现代风格)
                val stripeCount = 5
                for (i in 0 until stripeCount) {
                    // 上管道条纹
                    drawLine(
                        color = Color(0xFF81C784),
                        start = Offset(
                            x = pipe.position * size.width,
                            y = (pipe.gapPosition * size.height / stripeCount) * i
                        ),
                        end = Offset(
                            x = (pipe.position + pipeWidth) * size.width,
                            y = (pipe.gapPosition * size.height / stripeCount) * i
                        ),
                        strokeWidth = size.width * 0.005f
                    )

                    // 下管道条纹
                    drawLine(
                        color = Color(0xFF81C784),
                        start = Offset(
                            x = pipe.position * size.width,
                            y = (pipe.gapPosition + pipe.gapSize) * size.height +
                                    ((1 - pipe.gapPosition - pipe.gapSize) * size.height / stripeCount) * i
                        ),
                        end = Offset(
                            x = (pipe.position + pipeWidth) * size.width,
                            y = (pipe.gapPosition + pipe.gapSize) * size.height +
                                    ((1 - pipe.gapPosition - pipe.gapSize) * size.height / stripeCount) * i
                        ),
                        strokeWidth = size.width * 0.005f
                    )
                }

                // 管道顶部装饰 (现代风格)
                drawRoundRect(
                    color = Color(0xFF1B5E20),
                    topLeft = Offset(
                        x = pipe.position * size.width - size.width * 0.01f,
                        y = pipe.gapPosition * size.height - size.width * 0.03f
                    ),
                    size = Size(
                        width = pipeWidth * size.width + size.width * 0.02f,
                        height = size.width * 0.03f
                    ),
                    cornerRadius = CornerRadius(size.width * 0.015f)
                )

                // 管道底部装饰 (现代风格)
                drawRoundRect(
                    color = Color(0xFF1B5E20),
                    topLeft = Offset(
                        x = pipe.position * size.width - size.width * 0.01f,
                        y = (pipe.gapPosition + pipe.gapSize) * size.height
                    ),
                    size = Size(
                        width = pipeWidth * size.width + size.width * 0.02f,
                        height = size.width * 0.03f
                    ),
                    cornerRadius = CornerRadius(size.width * 0.015f)
                )
            }

            // 绘制小鸟
            if (gameState != GameState.NOT_STARTED) {
                val birdCenter = Offset(
                    x = size.width * 0.3f,
                    y = birdY.value * size.height
                )

                // 绘制小鸟身体
                drawCircle(
                    color = Color(0xFFFFD700),
                    center = birdCenter,
                    radius = size.width * birdSize
                )

                // 绘制小鸟眼睛
                drawCircle(
                    color = Color.White,
                    center = birdCenter + Offset(size.width * birdSize * 0.5f, -size.width * birdSize * 0.3f),
                    radius = size.width * birdSize * 0.2f
                )
                drawCircle(
                    color = Color.Black,
                    center = birdCenter + Offset(size.width * birdSize * 0.6f, -size.width * birdSize * 0.3f),
                    radius = size.width * birdSize * 0.1f
                )

                // 绘制小鸟嘴巴
                val path = Path().apply {
                    moveTo(birdCenter.x + size.width * birdSize * 0.5f, birdCenter.y)
                    lineTo(birdCenter.x + size.width * birdSize * 1.0f, birdCenter.y - size.width * birdSize * 0.2f)
                    lineTo(birdCenter.x + size.width * birdSize * 1.0f, birdCenter.y + size.width * birdSize * 0.2f)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFF5722)
                )

                // 绘制小鸟翅膀
                val wingPath = Path().apply {
                    moveTo(birdCenter.x - size.width * birdSize * 0.4f, birdCenter.y)
                    quadraticBezierTo(
                        birdCenter.x - size.width * birdSize * 0.8f,
                        birdCenter.y - size.width * birdSize * 0.8f,
                        birdCenter.x - size.width * birdSize * 0.3f,
                        birdCenter.y - size.width * birdSize * 0.5f
                    )
                    quadraticBezierTo(
                        birdCenter.x,
                        birdCenter.y - size.width * birdSize * 1.0f,
                        birdCenter.x + size.width * birdSize * 0.3f,
                        birdCenter.y - size.width * birdSize * 0.3f
                    )
                    close()
                }
                drawPath(
                    path = wingPath,
                    color = Color(0xFFFFA000)
                )
            }

            // 绘制分数
            if (gameState == GameState.RUNNING) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        score.toString(),
                        size.width / 2,
                        size.height * 0.1f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 80f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
                        }
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                }
        ) {
            // 绘制地面 (现代风格)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFA0522D), Color(0xFF8B4513)),
                    startY = size.height * 0.95f,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height * 0.95f),
                size = Size(size.width, size.height * 0.05f)
            )

            // 绘制地面纹理细节
            for (i in 0..15) {
                val xPos = i * (size.width / 15)
                drawLine(
                    color = Color(0xFF804000),
                    start = Offset(xPos, size.height * 0.95f),
                    end = Offset(xPos, size.height),
                    strokeWidth = size.width * 0.001f
                )
            }

            // 绘制草地 (现代风格)
            val grassHeight = size.height * 0.02f
            val waveHeight = grassHeight * 0.5f
            val waveLength = size.width / 20

            // 草地波浪路径
            val grassPath = Path().apply {
                moveTo(0f, size.height * 0.93f)
                for (i in 0..20) {
                    val x = i * waveLength
                    val y = size.height * 0.93f +
                            if (i % 2 == 0) -waveHeight else waveHeight
                    quadraticBezierTo(
                        x - waveLength/2, y,
                        x, y
                    )
                }
                lineTo(size.width, size.height * 0.93f + grassHeight)
                lineTo(0f, size.height * 0.93f + grassHeight)
                close()
            }

            // 绘制草地
            drawPath(
                path = grassPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7CFC00),
                        Color(0xFF3CB371)
                    ),
                    startY = size.height * 0.93f,
                    endY = size.height * 0.93f + grassHeight
                )
            )

            // 添加草地细节 - 草叶
            grassBlades.forEach { blade ->
                val x = blade.x * size.width
                val bladeHeight = blade.height * grassHeight
                val bladeWidth = bladeHeight * 0.2f

                val bladePath = Path().apply {
                    moveTo(x, size.height * 0.93f + grassHeight - bladeHeight)
                    quadraticBezierTo(
                        x + bladeWidth,
                        size.height * 0.93f + grassHeight - bladeHeight * 0.5f,
                        x,
                        size.height * 0.93f + grassHeight
                    )
                    quadraticBezierTo(
                        x - bladeWidth,
                        size.height * 0.93f + grassHeight - bladeHeight * 0.5f,
                        x,
                        size.height * 0.93f + grassHeight - bladeHeight
                    )
                }

                drawPath(
                    path = bladePath,
                    color = Color(0xFF2E8B57).copy(alpha = 0.7f),
                    style = Stroke(width = size.width * 0.002f)
                )
            }

            // 地面阴影
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent),
                    startY = size.height * 0.93f,
                    endY = size.height * 0.93f + grassHeight * 0.5f
                ),
                topLeft = Offset(0f, size.height * 0.93f),
                size = Size(size.width, grassHeight * 0.5f)
            )
        }

        // 游戏状态覆盖层
        when (gameState) {
            GameState.NOT_STARTED -> {
                NotStarted(bestScore)
            }

            GameState.GAME_OVER -> {
                GameOver(bestScore, score) { startGame() }
            }

            else -> {}
        }
    }
}

@Composable
fun NotStarted(bestScore: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Flappy Bird",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "点击屏幕开始游戏",
                fontSize = 20.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "最高分: $bestScore",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun GameOver(bestScore: Int, score: Int, startGame: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "游戏结束",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "得分: $score",
                fontSize = 24.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "最高分: $bestScore",
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { startGame() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)))
            {
                Text(text = "重新开始", fontSize = 18.sp)
            }
        }
    }
}

// 检查碰撞
fun checkCollision(
    birdY: Float,
    birdSize: Float,
    pipe: Pipe,
    pipeWidth: Float
): Boolean {
    // 小鸟碰撞矩形 (简化为一个矩形)
    val birdRect = Rect(
        left = 0.3f - birdSize / 2,
        top = birdY - birdSize / 2,
        right = 0.3f + birdSize / 2,
        bottom = birdY + birdSize / 2
    )

    // 上管道碰撞矩形
    val topPipeRect = Rect(
        left = pipe.position,
        top = 0f,
        right = pipe.position + pipeWidth,
        bottom = pipe.gapPosition
    )

    // 下管道碰撞矩形
    val bottomPipeRect = Rect(
        left = pipe.position,
        top = pipe.gapPosition + pipe.gapSize,
        right = pipe.position + pipeWidth,
        bottom = 1f
    )

    return birdRect.overlaps(topPipeRect) || birdRect.overlaps(bottomPipeRect)
}

// 游戏状态
enum class GameState {
    NOT_STARTED,
    RUNNING,
    GAME_OVER
}

// 管道数据类
data class Pipe(
    val position: Float, // 0-1 范围
    val gapPosition: Float, // 0-1 范围
    val gapSize: Float, // 0-1 范围
    var passed: Boolean
)

data class GrassBlade(val x: Float, val height: Float, val width: Float)

@Composable
fun FlappyBirdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme().copy(
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFFFFC107),
            background = Color(0xFF70C5CE)
        ),
        content = content
    )
}