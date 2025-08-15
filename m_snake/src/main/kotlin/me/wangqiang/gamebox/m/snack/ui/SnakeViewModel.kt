package me.wangqiang.gamebox.m.snack.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

// 游戏状态枚举
enum class GameState {
    Running, Paused, Ended
}

// 方向枚举
enum class Direction {
    Up, Down, Left, Right
}

// 网格坐标
data class Point(val x: Int, val y: Int)

// 蛇的数据结构
data class Snake(val body: List<Point>, val direction: Direction)

// 食物的数据结构
data class Food(val position: Point)

class SnakeViewModel : ViewModel() {
    companion object {
        const val GRID_SIZE = 20
        const val INIT_SNAKE_LENGTH = 3
    }

    // 游戏状态
    var gameState by mutableStateOf(GameState.Running)
        private set

    // 蛇
    var snake by mutableStateOf(initSnake())
        private set

    // 食物
    var food by mutableStateOf(generateFood(snake.body))
        private set

    // 方向控制
    fun changeDirection(newDirection: Direction) {
        Log.i("m_snack", "changeDirection: ${newDirection.name}")
        // 防止反向移动
        if ((snake.direction == Direction.Left && newDirection != Direction.Right) ||
            (snake.direction == Direction.Right && newDirection != Direction.Left) ||
            (snake.direction == Direction.Up && newDirection != Direction.Down) ||
            (snake.direction == Direction.Down && newDirection != Direction.Up)) {
            if (snake.direction != newDirection) {
                snake = snake.copy(direction = newDirection)
            }
        }
    }

    // 游戏主循环（可由Timer或协程驱动）
    fun step() {
        if (gameState != GameState.Running) return
        val nextHead = getNextHead(snake.body.first(), snake.direction)
        // 判断是否撞墙或撞自己
        if (nextHead.x !in 0 until GRID_SIZE || nextHead.y !in 0 until GRID_SIZE || snake.body.contains(nextHead)) {
            gameState = GameState.Ended
            return
        }
        val newBody = mutableListOf(nextHead)
        newBody.addAll(snake.body)
        // 吃到食物
        if (nextHead == food.position) {
            food = generateFood(newBody)
        } else {
            newBody.removeAt(newBody.lastIndex)
        }
        snake = snake.copy(body = newBody)
    }

    fun pauseGame() {
        if (gameState == GameState.Running) gameState = GameState.Paused
    }

    fun resumeGame() {
        if (gameState == GameState.Paused) gameState = GameState.Running
    }

    fun restartGame() {
        snake = initSnake()
        food = generateFood(snake.body)
        gameState = GameState.Running
    }

    private fun initSnake(): Snake {
        val startY = GRID_SIZE / 2
        val startX = GRID_SIZE / 2
        val body = List(INIT_SNAKE_LENGTH) { Point(startX - it, startY) }
        return Snake(body, Direction.Right)
    }

    private fun generateFood(snakeBody: List<Point>): Food {
        val emptyCells = mutableListOf<Point>()
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                val p = Point(x, y)
                if (!snakeBody.contains(p)) emptyCells.add(p)
            }
        }
        val pos = emptyCells[Random.nextInt(emptyCells.size)]
        return Food(pos)
    }

    private fun getNextHead(head: Point, direction: Direction): Point {
        return when (direction) {
            Direction.Up -> Point(head.x, head.y - 1)
            Direction.Down -> Point(head.x, head.y + 1)
            Direction.Left -> Point(head.x - 1, head.y)
            Direction.Right -> Point(head.x + 1, head.y)
        }
    }

    /**
     * 检查蛇头是否发生碰撞（撞墙或撞自己）
     * @param body 蛇身位置列表（第一个为蛇头）
     * @param gridSize 网格尺寸
     * @return true表示发生碰撞
     */
    fun isCollision(body: List<Point>, gridSize: Int): Boolean {
        val head = body.firstOrNull() ?: return false
        // 撞墙检测
        if (head.x !in 0 until gridSize || head.y !in 0 until gridSize) return true
        // 撞自己检测（跳过第一个节点）
        if (body.drop(1).any { it == head }) return true
        return false
    }
}