package me.wangqiang.gamebox.m.gobang.util

import android.util.Log
import me.wangqiang.gamebox.m.gobang.bean.Piece

class GomokuAI(private val maxDepth: Int = 2) {
    companion object {
        private fun buildScoreTable(): Map<String, Pair<Int, Int>> {
            // 不用下，已经赢了
            val p0 = Int.MAX_VALUE
            // 一步代价
            val p1 = 1000
            // 两步代价
            val p2 = 100
            val p3 = 10
            val p4 = 1

            val d4 = p1  // 死4价值
            val d3 = p2   // 死3价值
            val d2 = p3   // 死2价值
            val d1 = p4    // 死1价值
            val table = mutableMapOf<String, Pair<Int, Int>>().apply {
                // 自己即将要下一个子 to 对手下之后自己才能下
                // 必胜模式
                put("11111", p0 to p0)

                // 活四系列
                put("_1111_", p1 to d4)
                put("_111_1_", p1 to d3 + d1)
                put("_11_11_", p1 to d2*2)

                // 冲四系列
                put("21111_", p1 to 0)
                put("2111_1", p1 to d1)
                put("21_1112", p1 to 0)
                put("211_112", p1 to 0)

                // 活三系列
                put("__111__", p2 to d3)
                put("_1_11_", p2 to d2+d1)
                put("_1_1_1_", p2 to d2+d1)

                // 眠三系列
                put("2_111__", p2 to 0)
                put("2111__", p2 to 0)
                put("21_11_", p2 to 0)
                put("21__11_", p2 to d2)
                put("2__1112", p2 to 0)
                put("2_111_2", p2 to 0)
                put("21_11_2", p2 to 0)
                put("21_1_12", p2 to 0)
                put("211_1__", p2 to 0)
                put("211_1_2", p2 to 0)

                // 活二系列
                put("__11__", p3 to d2)
                put("_1_1_", p3 to d1*2)

                // 眠二系列
                put("211___", p3 to 0)
                put("21_1__", p3 to 0)
                put("21__1_", p3 to 0)

                // 活一系列
                put("__1__", p4 to d1)
            }

            // 添加反转模式
            val reversedTable = table.toMutableMap()
            table.keys.forEach { key ->
                reversedTable[key.reversed()] = table[key]!!
            }

            return reversedTable
        }
    }
    private var scoreTable: Map<String, Pair<Int, Int>> = buildScoreTable()
    private var mySymbol = Piece.BLACK
    private var opSymbol = Piece.WHITE

    fun findBestMove(board: Array<Array<Piece>>, player: Piece): Move {
        mySymbol = player
        opSymbol = player.opponent()
//        Log.i("gobang", "findBestMove mySymbol:  $mySymbol, opSymbol: $opSymbol, emptyCells: ${getEmptyCells(board)}")
        val bestMove = alphaBeta(board, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE, true, null)
        println("当前最佳移动：$bestMove \n")
        return Move(bestMove.x, bestMove.y)
    }

    private fun alphaBeta(
        board: Array<Array<Piece>>,
        depth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        lastMove: Cell?
    ): Move {
        if (depth == 0 || isTerminalNode(board, lastMove)) {
            val score = evaluate(board, maximizingPlayer, lastMove)
            return Move(score = score)
        }

        val emptyCells = getEmptyCells(board)

        var bestMove: Move
        if (maximizingPlayer) {
            bestMove = Move(score = -Int.MAX_VALUE)
            for ((x, y) in emptyCells) {
                if (board[x][y] == Piece.EMPTY) {
                    board[x][y] = mySymbol
                    val move = alphaBeta(board, depth - 1, alpha, beta, false, Cell(x, y))
                    board[x][y] = Piece.EMPTY
                    move.x = x
                    move.y = y
                    if (move.score > bestMove.score) {
                            bestMove = move
                    }
                    val maxAlpha = maxOf(alpha, bestMove.score)
//                    Log.i("gobang", "alphaBeta depth: $depth, move: $move, bestMove: $bestMove")
                    if (beta <= maxAlpha) {
                        break
                    }
                }
            }
        } else {
            bestMove = Move(score = Int.MAX_VALUE)
            for ((x, y) in emptyCells) {
                if (board[x][y] == Piece.EMPTY) {
                    board[x][y] = opSymbol
                    val move = alphaBeta(board, depth - 1, alpha, beta, true, Cell(x, y))
                    board[x][y] = Piece.EMPTY
                    move.x = x
                    move.y = y
                    if (move.score < bestMove.score) {
                        bestMove = move
                    }
                    val minBeta = minOf(beta, bestMove.score)
//                    Log.i("gobang", "alphaBeta depth: $depth, move: $move, bestMove: $bestMove ")
                    if (minBeta <= alpha) {
                        break
                    }
                }
            }
        }
        return bestMove
    }

    // 优化后的候选位置生成（只搜索有棋子的周围位置）
    private fun getEmptyCells(board: Array<Array<Piece>>): List<Cell> {
        val candidates = mutableSetOf<Cell>()
        val radius = 1 // 搜索半径

        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] != Piece.EMPTY) {
                    for (dx in -radius..radius) {
                        for (dy in -radius..radius) {
                            val x = i + dx
                            val y = j + dy
                            if (x in board.indices && y in board[x].indices && board[x][y] == Piece.EMPTY) {
                                candidates.add(Cell(x, y))
                            }
                        }
                    }
                }
            }
        }
        return candidates.toList()
    }

    private fun evaluate(board: Array<Array<Piece>>, isMyTurn: Boolean, lastMove: Cell?): Int {
        val (x, y) = lastMove!!
        if (isWinningMove(board, x, y)) {
            return if (mySymbol == board[x][y]) Int.MAX_VALUE else Int.MIN_VALUE
        }
//        Log.i("gobang", "evaluate x: $x, y: $y: ")
        return evaluateNormal(board, isMyTurn)
    }

    private fun evaluateNormal(board: Array<Array<Piece>>, isMy: Boolean): Int {
        var totalScore = 0
        // 横向扫描
        for (row in board) {
            if (row.any { it != Piece.EMPTY }) {
                totalScore += evaluateLine(row.toList(), isMy)
            }
        }
        // 纵向扫描 (每一列)
        for (col in 0 until 15) {
            val column = (0 until 15).map { board[it][col] }
            if (column.any { it != Piece.EMPTY }) {
                totalScore += evaluateLine(column, isMy)
            }
        }

        // 对角线扫描（优化实现）
        fun scanDiagonal(startX: Int, startY: Int, dx: Int, dy: Int) {
            var x = startX
            var y = startY
            val line = mutableListOf<Piece>()
            while (x in 0 until 15 && y in 0 until 15) {
                line.add(board[y][x])
                x += dx
                y += dy
            }
            if (line.any { it != Piece.EMPTY }) {
                totalScore += evaluateLine(line, isMy)
            }
        }

        // 扫描所有对角线
        for (i in 0 until 15) {
            scanDiagonal(i, 0, 1, 1)    // 左下到右上
            scanDiagonal(0, i, 1, 1)
            scanDiagonal(i, 0, -1, 1)   // 右下到左上
            scanDiagonal(15 - 1, i, -1, 1)
        }
//        Log.i("gobang", "evaluateNormal totalScore: $totalScore")
        return totalScore
    }

    private fun evaluateLine(line: List<Piece>, isMy: Boolean): Int {
        if (line.size < 5) return 0

        val myLine = buildString {
            line.forEach {
                append(when (it) {
                    mySymbol -> '1'
                    opSymbol -> '2'
                    else -> '_'
                })
            }
        }

        val opLine = buildString {
            line.forEach {
                append(when (it) {
                    mySymbol -> '2'
                    opSymbol -> '1'
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
//        Log.i("gobang", "evaluateLine isMy: $isMy, myLine: $myLine, opLine: $opLine, score: $score")

        return score
    }

    fun isTerminalNode(board: Array<Array<Piece>>, lastMove: Cell?): Boolean {
        // 条件1：检查是否有玩家获胜
        val (x, y) = lastMove ?: return false
        if (isWinningMove(board, x, y)) return true

        // 条件2：检查棋盘是否已满
        return board.all { row -> row.none { it == Piece.EMPTY } }
    }

    private fun isWinningMove(board: Array<Array<Piece>>, lastX: Int, lastY: Int): Boolean {
        val directions = arrayOf(
            intArrayOf(1, 0),  // 垂直
            intArrayOf(0, 1),  // 水平
            intArrayOf(1, 1),  // 正斜线
            intArrayOf(1, -1)  // 反斜线
        )

        val player = board[lastX][lastY]
        if (player == Piece.EMPTY) return false

        for ((dx, dy) in directions) {
            var count = 1

            var x = lastX + dx
            var y = lastY + dy
            while (x in board.indices && y in board.indices && board[x][y] == player) {
                count++
                x += dx
                y += dy
            }

            x = lastX - dx
            y = lastY - dy
            while (x in board.indices && y in board.indices && board[x][y] == player) {
                count++
                x -= dx
                y -= dy
            }

            if (count >= 5) return true
        }

        return false
    }

    data class Cell(val x: Int, val y: Int)
    data class Move(var x: Int? = null, var y: Int? = null, var score: Int = 0)
}