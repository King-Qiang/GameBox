package me.wangqiang.gamebox.m.tictactoe.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import me.wangqiang.c.common.mvvm.BaseViewModel
import me.wangqiang.gamebox.m.tictactoe.bean.GameMove
import me.wangqiang.gamebox.m.tictactoe.util.TicTacToeAI

class TicTacToeViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        const val EMPTY = 0
        const val PLAYER_O = 1
        const val PLAYER_X = 2
        const val DRAW = 3
    }
    // 棋盘状态：0 - 空，1 - AI（O），2 - 玩家（X）
    private var board = Array(3) { IntArray(3) { EMPTY } }
    // AI 实例
    private val ai = TicTacToeAI()
    val mGameMove = MutableLiveData<GameMove>() // 当前选中的区号位置

    // 记录胜利和平局次数
    private var playerXWins = 0
    private var playerOWins = 0
    private var draws = 0

    // 可以通过 LiveData 暴露给 UI
    val scoreLiveData = MutableLiveData<Triple<Int, Int, Int>>().apply {
        postValue(Triple(playerXWins, playerOWins, draws))
    }

    // 更新分数的方法
    fun updateScores(result: Int) {
        when (result) {
            PLAYER_X -> playerXWins++
            PLAYER_O -> playerOWins++
            DRAW -> draws++
        }
        scoreLiveData.postValue(Triple(playerXWins, playerOWins, draws))
    }

    fun humanMove(x: Int, y: Int) {
        board[x][y] = PLAYER_X
        mGameMove.postValue(GameMove(x, y, PLAYER_X))
    }

    fun clickValid(x: Int, y: Int): Boolean {
        return board[x][y] == EMPTY
    }

    // AI 落子
    fun aiMove() {
        val move = ai.action(board)
        if (move != null) {
            if (move.x != null && move.y != null) {
                board[move.x!!][move.y!!] = PLAYER_O
                mGameMove.postValue(GameMove(move.x!!, move.y!!, PLAYER_O))
            }
        }
    }

    fun resetGame() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = EMPTY
            }
        }
    }

    fun isGameOver(): Boolean {
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

    fun isBoardFull(): Boolean {
        for (x in 0..2) {
            for (y in 0..2) {
                if (board[x][y] == EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    fun checkGameResult(): Int {
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

}