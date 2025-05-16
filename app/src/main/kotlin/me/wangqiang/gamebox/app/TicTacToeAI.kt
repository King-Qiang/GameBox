package me.wangqiang.gamebox.app

class TicTacToeAI {
    companion object {
        const val TEMP_SYMBOL = 0
        const val MY_SYMBOL = 1
        const val OP_SYMBOL = 2
    }

    fun action(board: Array<IntArray>): Move? {
        val bestMove = alphaBeta(board, -Int.MAX_VALUE, Int.MAX_VALUE, true)
        println("当前最佳移动：$bestMove \n")
        return if (bestMove != null) {
            Move(bestMove.x, bestMove.y)
        } else {
            null
        }
    }

    private fun alphaBeta(board: Array<IntArray>, alpha: Int, beta: Int, maximizingPlayer: Boolean): Move? {
        val emptyCells = getEmptyCells(board)

        if (isGameOver(board) || emptyCells.isEmpty()) {
            val score = evaluate(board)
            return Move(score = score)
        }

        var bestMove: Move?
        if (maximizingPlayer) {
            bestMove = Move(score = -Int.MAX_VALUE)
            for (cell in emptyCells) {
                val (x, y) = cell
                board[x][y] = MY_SYMBOL
                val move = alphaBeta(board, alpha, beta, false)
                board[x][y] = TEMP_SYMBOL
                move?.x = x
                move?.y = y
                if (move != null && move.score > bestMove!!.score) {
                    bestMove = move
                }
                val maxAlpha = maxOf(alpha, bestMove!!.score)
                if (beta <= maxAlpha) {
                    break
                }
            }
        } else {
            bestMove = Move(score = Int.MAX_VALUE)
            for (cell in emptyCells) {
                val (x, y) = cell
                board[x][y] = OP_SYMBOL
                val move = alphaBeta(board, alpha, beta, true)
                board[x][y] = TEMP_SYMBOL
                move?.x = x
                move?.y = y
                if (move != null && move.score < bestMove!!.score) {
                    bestMove = move
                }
                val minBeta = minOf(beta, bestMove!!.score)
                if (minBeta <= alpha) {
                    break
                }
            }
        }
        return bestMove
    }

    private fun getEmptyCells(board: Array<IntArray>): List<Cell> {
        val emptyCells = mutableListOf<Cell>()
        for (x in 0..2) {
            for (y in 0..2) {
                if (board[x][y] == TEMP_SYMBOL) {
                    emptyCells.add(Cell(x, y))
                }
            }
        }
        return emptyCells
    }

    private fun isGameOver(board: Array<IntArray>): Boolean {
        // 检查行
        for (i in 0..2) {
            if (board[i][0] != TEMP_SYMBOL && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true
            }
        }

        // 检查列
        for (i in 0..2) {
            if (board[0][i] != TEMP_SYMBOL && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true
            }
        }

        // 检查对角线
        if (board[0][0] != TEMP_SYMBOL && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true
        }
        return board[0][2] != TEMP_SYMBOL && board[0][2] == board[1][1] && board[1][1] == board[2][0]
    }

    private fun evaluate(board: Array<IntArray>): Int {
        if (isWinning(board, MY_SYMBOL)) {
            return 1
        } else if (isWinning(board, OP_SYMBOL)) {
            return -1
        } else {
            return 0
        }
    }

    private fun isWinning(board: Array<IntArray>, symbol: Int): Boolean {
        // 检查行
        for (i in 0..2) {
            if (board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) {
                return true
            }
        }

        // 检查列
        for (i in 0..2) {
            if (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol) {
                return true
            }
        }

        // 检查对角线
        if (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) {
            return true
        }
        return board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol
    }

    data class Cell(val x: Int, val y: Int)
    data class Move(var x: Int? = null, var y: Int? = null, var score: Int = 0)
}
