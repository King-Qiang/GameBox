package me.wangqiang.gamebox.m.gobang.view

class GomokuGame(val boardSize: Int = 15)  {
    enum class Piece { EMPTY, BLACK, WHITE }

    private val board = Array(boardSize) { Array(boardSize) { Piece.EMPTY } }
    var currentPlayer = Piece.BLACK
    var isGameOver = false
    var winner: Piece = Piece.EMPTY
        private set

    fun getPiece(row: Int, col: Int) = board[row][col]

    fun placePiece(row: Int, col: Int): Boolean {
        if (isGameOver || board[row][col] != Piece.EMPTY) return false
        board[row][col] = currentPlayer
        if (checkWin(row, col)) {
            isGameOver = true
            winner = currentPlayer
        } else {
            currentPlayer = if (currentPlayer == Piece.BLACK) Piece.WHITE else Piece.BLACK
        }
        return true
    }

    private fun checkWin(row: Int, col: Int): Boolean {
        val directions = arrayOf(
            intArrayOf(1, 0),   // 垂直
            intArrayOf(0, 1),   // 水平
            intArrayOf(1, 1),   // 右下
            intArrayOf(1, -1)   // 左下
        )

        return directions.any { (dx, dy) ->
            var count = 1
            var x = row + dx
            var y = col + dy
            while (x in 0 until boardSize && y in 0 until boardSize && board[x][y] == currentPlayer) {
                count++
                x += dx
                y += dy
            }

            x = row - dx
            y = col - dy
            while (x in 0 until boardSize && y in 0 until boardSize && board[x][y] == currentPlayer) {
                count++
                x -= dx
                y -= dy
            }

            count >= 5
        }
    }

    fun reset() {
        board.forEach { it.fill(Piece.EMPTY) }
        currentPlayer = Piece.BLACK
        isGameOver = false
        winner = Piece.EMPTY
    }
}