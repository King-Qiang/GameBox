package me.wangqiang.gamebox.app

class TicTacToeGame {
    companion object {
        const val EMPTY = 0
        const val PLAYER_X = 1
        const val PLAYER_O = 2
        const val DRAW = 3
    }

    private val board = Array(3) { IntArray(3) { EMPTY } }
    private val ai = TicTacToeAI()

    private var playerScore = 0
    private var aiScore = 0
    private var draws = 0

    fun start() {
        println("井字棋游戏开始!")
        println("你将是玩家 O，AI 将是玩家 X")

        var playAgain = true

        while (playAgain) {
            resetBoard()
            playGame()

            printBoard()
            val result = checkGameResult()

            when (result) {
                PLAYER_O -> {
                    println("恭喜! 你赢了!")
                    playerScore++
                }
                PLAYER_X -> {
                    println("遗憾! AI 赢了!")
                    aiScore++
                }
                DRAW -> {
                    println("平局!")
                    draws++
                }
                else -> println("游戏结束")
            }

            println("\n当前比分 - 玩家: $playerScore, AI: $aiScore, 平局: $draws")
            playAgain = askPlayAgain()
        }

        println("感谢游玩！再见!")
    }

    private fun playGame() {
        var currentPlayer = PLAYER_O
        var gameOver = false

        while (!gameOver) {
            printBoard()

            if (currentPlayer == PLAYER_O) {
                // 人类玩家回合
                val move = getHumanMove()
                board[move.x!!][move.y!!] = currentPlayer
            } else {
                // AI玩家回合
                val move = ai.action(board)
                if (move != null) {
                    board[move.x!!][move.y!!] = currentPlayer
                }
            }

            currentPlayer = if (currentPlayer == PLAYER_X) PLAYER_O else PLAYER_X

            // 检查游戏是否结束
            if (isGameOver()) {
                gameOver = true
            }

            // 检查平局
            if (isBoardFull() && !gameOver) {
                gameOver = true
            }
        }
    }

    private fun getHumanMove(): TicTacToeAI.Move {
        while (true) {
            print("请输入你的着法 (格式为 x,y，例如: 1,2): ")
            val input = readLine()

            if (input != null && input.matches(Regex("^\\s*[0-2]\\s*,\\s*[0-2]\\s*\$"))) {
                val (x, y) = input.split(",").map { it.trim().toInt() }

                if (board[x][y] == EMPTY) {
                    return TicTacToeAI.Move(x, y)
                } else {
                    println("该位置已被占据，请重新选择")
                }
            } else {
                println("输入无效，请按照指定格式输入坐标（0-2）")
            }
        }
    }

    private fun printBoard() {
        println("\n当前棋盘:")
        for (x in 0..2) {
            for (y in 0..2) {
                val symbol = when (board[x][y]) {
                    PLAYER_X -> "X"
                    PLAYER_O -> "O"
                    else -> " "
                }
                print(symbol)
                if (y < 2) print(" | ")
            }
            println()
            if (x < 2) println("-----------")
        }
        println()
    }

    private fun isGameOver(): Boolean {
        // 检查行
        for (i in 0..2) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true
            }
        }

        // 检查列
        for (i in 0..2) {
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true
            }
        }

        // 检查对角线
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true
        }
        return board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]
    }

    private fun isBoardFull(): Boolean {
        for (x in 0..2) {
            for (y in 0..2) {
                if (board[x][y] == EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkGameResult(): Int {
        // 检查是否有胜者
        for (i in 0..2) {
            // 检查行
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }

            // 检查列
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }

        // 检查对角线
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }

        // 如果棋盘已满且无胜者，则为平局
        if (isBoardFull()) {
            return DRAW
        }

        // 游戏尚未结束
        return EMPTY
    }

    private fun resetBoard() {
        for (x in 0..2) {
            for (y in 0..2) {
                board[x][y] = EMPTY
            }
        }
    }

    private fun askPlayAgain(): Boolean {
        while (true) {
            print("\n想再玩一次吗? (Y/N): ")
            val input = readLine()?.trim()?.toUpperCase()

            if (input == "Y" || input == "YES") {
                return true
            } else if (input == "N" || input == "NO") {
                return false
            } else {
                println("请输入 Y 或 N")
            }
        }
    }
}

fun main() {
    val game = TicTacToeGame()
    game.start()
}
