package me.wangqiang.gamebox.m.snack.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alibaba.android.arouter.facade.annotation.Route
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_Snake
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.wangqiang.gamebox.m.snack.ui.SnakeGameScreen
import me.wangqiang.gamebox.m.snack.ui.SnakeViewModel
import me.wangqiang.gamebox.m.snack.ui.Direction
import me.wangqiang.gamebox.m.snack.ui.GameState

@Route(path = RoutePath_Snake.Page.Snake_Main)
class SnakeActivity : ComponentActivity() {
    private val viewModel: SnakeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val snake = viewModel.snake.body
            val food = viewModel.food.position
            val gridSize = SnakeViewModel.GRID_SIZE
            val score = snake.size - SnakeViewModel.INIT_SNAKE_LENGTH
            val gameState = viewModel.gameState
            SnakeGameScreen(
                snake = snake,
                food = food,
                gridSize = gridSize,
                score = score,
                gameState = gameState,
                onDirectionChange = { dir -> viewModel.changeDirection(dir) },
                onRestart = { viewModel.restartGame() },
                onResume = { viewModel.resumeGame() },
                onPause = { viewModel.pauseGame() },
                onStep = { viewModel.step() },
                targetFps = 60
            )
        }
    }
}