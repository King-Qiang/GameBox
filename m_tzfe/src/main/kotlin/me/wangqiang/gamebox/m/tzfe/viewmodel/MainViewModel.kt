package me.wangqiang.gamebox.m.tzfe.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import me.wangqiang.c.common.mvvm.BaseViewModel
import me.wangqiang.gamebox.m.tzfe.bean.Direction
import me.wangqiang.gamebox.m.tzfe.bean.Tile
import me.wangqiang.gamebox.m.tzfe.bean.TileMovement
import kotlin.random.Random

class MainViewModel(application: Application) : BaseViewModel(application) {
    private val grid: Array<Array<Tile>> = Array(4) { Array(4) { Tile() } }
    var lastMovements: MutableList<TileMovement> = mutableListOf()
    private var score = 0
    val gameUiState = MutableLiveData<GameUiState>(GameUiState.Playing)
    val gameGrid: MutableLiveData<Array<Array<Tile>>> = MutableLiveData()
    val movementsLiveData: MutableLiveData<List<TileMovement>> = MutableLiveData()
    val currentScore = MutableLiveData<Int>(0)
    val highestScore = MutableLiveData<Int>(0)

    init {
        reset()
    }

    fun getGrid(): Array<Array<Tile>> {
        return grid
    }

    fun reset() {
        // 清空网格
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                grid[i][j] = Tile()
            }
        }
        // 生成两个初始方块
        addNewNumber()
        addNewNumber()
        currentScore.postValue(0)
        if (score >  highestScore.value!!) {
            highestScore.postValue(score)
        }
        score = 0
        gameGrid.postValue(grid)
    }

    fun handleMove(direction: Direction) {
        if (gameUiState.value !is GameUiState.Playing) return
        val moved = move(direction)
        if (moved) {
            movementsLiveData.postValue(lastMovements)
            addNewNumber()
            checkGameState()
            currentScore.postValue(score)
//            gameGrid.postValue(Array(4) { Array(4) { Tile() } })
        }
    }

    private fun checkGameState() {
        if (isGameOver()) {
            gameUiState.value = GameUiState.GameOver(500)
            if (score > highestScore.value!!) {
                highestScore.postValue(score)
            }
        } else if (grid.any { row -> row.any { it.value == 2048 } }) {
            gameUiState.value = GameUiState.Win(10000)
        }
    }

    private fun addNewNumber() {
        val emptyCells = mutableListOf<Pair<Int, Int>>().apply {
            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    if (grid[i][j].value == 0) add(i to j)
                }
            }
        }
        emptyCells.randomOrNull()?.let { (x, y) ->
            grid[x][y] = if (Random.nextInt(10) < 9) Tile(2, null) else Tile(4, null)
        }
    }

    // 完整移动逻辑（以左移为例）
    private fun move(direction: Direction): Boolean {
        val oldGrid = deepCopyGrid()
        resetMergeFlags()
        when (direction) {
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
            Direction.UP -> moveUp()
            Direction.DOWN -> moveDown()
        }
        return !grid.contentDeepEquals(oldGrid)
    }

    private fun moveLeft() {
        for (i in 0 until 4) {
            var writePos = 0
            val merged = BooleanArray(4) { false } // 记录该位置是否已合并

            for (j in 0 until 4) {
                val current = grid[i][j]
                if (current.value == 0) continue

                if (writePos > 0 && grid[i][writePos - 1].value == current.value && !merged[writePos - 1]) {
                    // 合并相同值
                    val mergedValue = current.value * 2
                    grid[i][writePos - 1] = Tile(value = mergedValue).apply {
                        mergedFrom = grid[i][writePos - 1] to current
                    }
                    grid[i][j] = Tile()
                    lastMovements.add(TileMovement(i, j, i, writePos - 1, true))
                    score += mergedValue
                    merged[writePos - 1] = true // 标记已合并
                } else {
                    if (j != writePos) {
                        grid[i][writePos] = current
                        grid[i][j] = Tile()
                        lastMovements.add(TileMovement(i, j, i, writePos))
                    }
                    writePos++
                }
            }
        }
    }

    // 其他方向移动逻辑（结构类似，调整遍历顺序）
    private fun moveRight() { /* 反向遍历 */
        for (i in 0 until 4) {
            var writePos = 3
            val merged = BooleanArray(4) { false }

            for (j in 3 downTo 0) {
                val current = grid[i][j]
                if (current.value == 0) continue

                if (writePos < 3 && grid[i][writePos + 1].value == current.value && !merged[writePos + 1]) {
                    val mergedValue = current.value * 2
                    grid[i][writePos + 1] = Tile(value = mergedValue).apply {
                        mergedFrom = grid[i][writePos + 1] to current
                    }
                    grid[i][j] = Tile()
                    lastMovements.add(TileMovement(i, j, i, writePos + 1, true))
                    score += mergedValue
                    merged[writePos + 1] = true
                } else {
                    if (j != writePos) {
                        grid[i][writePos] = current
                        grid[i][j] = Tile()
                        lastMovements.add(TileMovement(i, j, i, writePos))
                    }
                    writePos--
                }
            }
        }
    }
    private fun moveUp() { /* 按列处理 */
        for (j in 0 until 4) {
            var writePos = 0
            val merged = BooleanArray(4) { false }

            for (i in 0 until 4) {
                val current = grid[i][j]
                if (current.value == 0) continue

                if (writePos > 0 && grid[writePos - 1][j].value == current.value && !merged[writePos - 1]) {
                    val mergedValue = current.value * 2
                    grid[writePos - 1][j] = Tile(value = mergedValue).apply {
                        mergedFrom = grid[writePos - 1][j] to current
                    }
                    grid[i][j] = Tile()
                    lastMovements.add(TileMovement(i, j, writePos - 1, j, true))
                    score += mergedValue
                    merged[writePos - 1] = true
                } else  {
                    if (i != writePos) {
                        grid[writePos][j] = current
                        grid[i][j] = Tile()
                        lastMovements.add(TileMovement(i, j, writePos, j))
                    }
                    writePos++
                }
            }
        }
    }
    private fun moveDown() { /* 反向按列处理 */
        for (j in 0 until 4) {
            var writePos = 3
            val merged = BooleanArray(4) { false }
            for (i in 3 downTo 0) {
                val current = grid[i][j]
                if (current.value == 0) continue
                if (writePos < 3 && grid[writePos + 1][j].value == current.value && !merged[writePos + 1]) {
                    val mergedValue = current.value * 2
                    grid[writePos + 1][j] = Tile(value = mergedValue).apply {
                        mergedFrom = grid[writePos + 1][j] to current
                    }
                    grid[i][j] = Tile()
                    lastMovements.add(TileMovement(i, j, writePos + 1, j, true))
                    score += mergedValue
                    merged[writePos + 1] = true
                } else {
                    if (i != writePos) {
                        grid[writePos][j] = current
                        grid[i][j] = Tile()
                        lastMovements.add(TileMovement(i, j, writePos, j))
                    }
                    writePos--
                }
            }
        }
    }

    private fun resetMergeFlags() {
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                grid[i][j].mergedFrom = null
            }
        }
        lastMovements.clear()
    }

    private fun deepCopyGrid(): Array<Array<Tile>> {
        return Array(4) { i ->
            Array(4) { j ->
                grid[i][j]
            }
        }
    }

    fun isGameOver(): Boolean {
        // 检查空单元格
        if (hasZero()) return false

        // 检查相邻可合并
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                val current = grid[i][j]
                if ((i > 0 && grid[i-1][j] == current) ||
                    (i < 4-1 && grid[i+1][j] == current) ||
                    (j > 0 && grid[i][j-1] == current) ||
                    (j < 4-1 && grid[i][j+1] == current)) {
                    return false
                }
            }
        }
        return true
    }

    private fun hasZero(): Boolean {
        return grid.any { row ->
            row.any { it.value == 0 }
        }
    }
}

sealed class GameUiState {
    data object Playing : GameUiState()
    data class Win(val score: Int) : GameUiState()
    data class GameOver(val score: Int) : GameUiState()
}