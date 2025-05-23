package me.wangqiang.gamebox.app

import me.wangqiang.gamebox.app.GomokuAI.Companion.INF
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GomokuAI {
    companion object {
        const val TEMP_SYMBOL = 0
        const val MY_SYMBOL = 1
        const val OP_SYMBOL = 2
        const val MAX_DEEP = 2
        const val INF = 999999999
        const val BOARD_SIZE = 15
        private fun buildScoreTable(): Map<String, Pair<Int, Int>> {
            val table = mutableMapOf<String, Pair<Int, Int>>().apply {
                // 必胜模式
                put("11111", INF to INF)

                // 活四系列
                put("_1111_", 50000 to 20000)
                put("_111_1_", 40000 to 15000)
                put("_11_11_", 35000 to 12000)

                // 冲四系列
                put("21111_", 10000 to 5000)
                put("_11112", 10000 to 5000)
                put("2111_1", 8000 to 4000)
                put("1_1112", 8000 to 4000)

                // 活三系列
                put("__111__", 3000 to 1500)
                put("_1_11_", 2500 to 1200)
                put("_11_1_", 2500 to 1200)

                // 眠三系列
                put("2111__", 800 to 400)
                put("__1112", 800 to 400)
                put("21_11_", 600 to 300)

                // 活二系列
                put("__11__", 150 to 80)
                put("_1_1_", 100 to 50)
                put("_1__1_", 80 to 40)

                // 眠二系列
                put("211___", 30 to 15)
                put("___112", 30 to 15)
            }

            // 添加反转模式
            val reversedTable = table.toMutableMap()
            table.keys.forEach { key ->
                reversedTable[key.reversed()] = table[key]!!
            }

            // 特殊模式补充
            reversedTable.putAll(mapOf(
                "1_1_1" to Pair(1200, 600),
                "1__1_1" to Pair(1000, 500),
                "_1_1_1_" to Pair(2000, 1000)
            ))

            return reversedTable
        }
    }

    data class Pos(val x: Int, val y: Int)
    data class Move(val score: Int, val x: Int, val y: Int)

    private var scoreTable: Map<String, Pair<Int, Int>> = buildScoreTable()

    fun action(board: Array<IntArray>): Pos {
        // 优先占中
        if (board[7][7] == TEMP_SYMBOL) return Pos(7, 7)

        val bestMove = alphaBeta(board, -INF, INF, true, MAX_DEEP)
        return Pos(bestMove.x, bestMove.y)
    }

    private fun alphaBeta(
        board: Array<IntArray>,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        depth: Int
    ): Move {
        val emptyCells = getRoundCells(board)

        if (isGameOver(board) || emptyCells.isEmpty() || depth == 0) {
            return Move(evaluate(board, maximizingPlayer), -1, -1)
        }

        var currentAlpha = alpha
        var currentBeta = beta
        var bestMove = if (maximizingPlayer) Move(-INF, -1, -1) else Move(INF, -1, -1)

        for (cell in emptyCells) {
            val originalValue = board[cell.y][cell.x]

            // 模拟落子
            board[cell.y][cell.x] = if (maximizingPlayer) MY_SYMBOL else OP_SYMBOL

            val currentMove = alphaBeta(
                board,
                currentAlpha,
                currentBeta,
                !maximizingPlayer,
                depth - 1
            ).copy(x = cell.x, y = cell.y)

            // 回溯
            board[cell.y][cell.x] = originalValue

            if (maximizingPlayer) {
                if (currentMove.score > bestMove.score) {
                    bestMove = currentMove
                    currentAlpha = max(currentAlpha, bestMove.score)
                }
            } else {
                if (currentMove.score < bestMove.score) {
                    bestMove = currentMove
                    currentBeta = min(currentBeta, bestMove.score)
                }
            }

            if (currentAlpha >= currentBeta) break
        }

        return bestMove
    }

    private fun getRoundCells(board: Array<IntArray>): List<Pos> {
        val cells = mutableSetOf<Pos>()
        val directions = arrayOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1,        0 to 1,
            1 to -1,  1 to 0,  1 to 1
        )

        for (y in 0 until BOARD_SIZE) {
            for (x in 0 until BOARD_SIZE) {
                if (board[y][x] != TEMP_SYMBOL) {
                    directions.forEach { (dy, dx) ->
                        val ny = y + dy
                        val nx = x + dx
                        if (nx in 0 until BOARD_SIZE && ny in 0 until BOARD_SIZE
                            && board[ny][nx] == TEMP_SYMBOL) {
                            cells.add(Pos(nx, ny))
                        }
                    }
                }
            }
        }
        return if (cells.isEmpty()) listOf(Pos(7, 7)) else cells.toList()
    }

    private fun isGameOver(board: Array<IntArray>): Boolean {
        // 简化实现：只需检查是否存在五连
        fun checkSequence(x: Int, y: Int, dx: Int, dy: Int): Boolean {
            val symbol = board[y][x]
            if (symbol == TEMP_SYMBOL) return false
            for (i in 1..4) {
                val nx = x + dx * i
                val ny = y + dy * i
                if (nx !in 0 until BOARD_SIZE || ny !in 0 until BOARD_SIZE
                    || board[ny][nx] != symbol) {
                    return false
                }
            }
            return true
        }

        for (y in 0 until BOARD_SIZE) {
            for (x in 0 until BOARD_SIZE) {
                if (checkSequence(x, y, 1, 0) ||  // 水平
                    checkSequence(x, y, 0, 1) ||  // 垂直
                    checkSequence(x, y, 1, 1) ||  // 正对角线
                    checkSequence(x, y, 1, -1)) { // 反对角线
                    return true
                }
            }
        }
        return false
    }

    private fun evaluate(board: Array<IntArray>, isMyTurn: Boolean): Int {
        if (isWinning(board, MY_SYMBOL)) return INF
        if (isWinning(board, OP_SYMBOL)) return -INF
        return evaluateNormal(board, isMyTurn)
    }

    private fun evaluateNormal(board: Array<IntArray>, isMy: Boolean): Int {
        var totalScore = 0

        // 横向扫描
        for (y in 0 until BOARD_SIZE) {
            totalScore += evaluateLine(board[y].toList(), isMy)
        }

        // 纵向扫描
        for (x in 0 until BOARD_SIZE) {
            val column = (0 until BOARD_SIZE).map { board[it][x] }
            totalScore += evaluateLine(column, isMy)
        }

        // 对角线扫描（优化实现）
        fun scanDiagonal(startX: Int, startY: Int, dx: Int, dy: Int) {
            var x = startX
            var y = startY
            val line = mutableListOf<Int>()
            while (x in 0 until BOARD_SIZE && y in 0 until BOARD_SIZE) {
                line.add(board[y][x])
                x += dx
                y += dy
            }
            totalScore += evaluateLine(line, isMy)
        }

        // 扫描所有对角线
        for (i in 0 until BOARD_SIZE) {
            scanDiagonal(i, 0, 1, 1)    // 左下到右上
            scanDiagonal(0, i, 1, 1)
            scanDiagonal(i, 0, -1, 1)   // 右下到左上
            scanDiagonal(BOARD_SIZE - 1, i, -1, 1)
        }

        return totalScore
    }

    private fun evaluateLine(line: List<Int>, isMy: Boolean): Int {
        if (line.size < 5) return 0

        val myLine = buildString {
            line.forEach {
                append(when (it) {
                    MY_SYMBOL -> '1'
                    OP_SYMBOL -> '2'
                    else -> '_'
                })
            }
        }

        val opLine = buildString {
            line.forEach {
                append(when (it) {
                    MY_SYMBOL -> '2'
                    OP_SYMBOL -> '1'
                    else -> '_'
                })
            }
        }

        var score = 0
        scoreTable.forEach { entry ->
            val pattern = entry.key
            val (myScore, opScore) = entry.value

            when {
                myLine.contains(pattern) -> score += if (isMy) myScore else opScore
                opLine.contains(pattern) -> score -= if (isMy) opScore else myScore
            }
        }

        return score
    }

    private fun isWinning(board: Array<IntArray>, symbol: Int): Boolean {
        return isGameOver(board) && board.any { row -> row.any { it == symbol } }
    }
}