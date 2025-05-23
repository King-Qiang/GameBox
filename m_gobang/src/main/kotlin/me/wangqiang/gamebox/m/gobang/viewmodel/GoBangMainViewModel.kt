package me.wangqiang.gamebox.m.gobang.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.wangqiang.c.common.mvvm.BaseViewModel
import me.wangqiang.gamebox.m.gobang.bean.GameMove
import me.wangqiang.gamebox.m.gobang.bean.MovePair
import me.wangqiang.gamebox.m.gobang.bean.Piece
import me.wangqiang.gamebox.m.gobang.util.GomokuAI
import java.util.Stack

class GoBangMainViewModel(application: Application): BaseViewModel(application) {
    private val ai = GomokuAI()
    private var board = Array(15) { Array(15) { Piece.EMPTY } }
    val mUndoGameMove = MutableLiveData<MovePair>()
    val mGameMove = MutableLiveData<GameMove>()
    val canUndo = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    // 可以通过 LiveData 暴露给 UI
    val scoreLiveData = MutableLiveData<Triple<Int, Int, Int>>().apply {
        postValue(Triple(aiWins, playerWins, draw))
    }
    // 记录胜利和平局次数
    private var aiWins = 0
    private var playerWins = 0
    private var draw = 0
    private var isGameOver = false
    private var aiThinking = false
    private val moveHistory = Stack<MovePair>()
    private var humanSymbol = Piece.WHITE // 玩家先手
    private var aiSymbol = Piece.BLACK // 电脑后手
    private var pendingHumanMove: GameMove? = null

    fun isAiThinking(): Boolean {
        return aiThinking
    }

    fun isGameOver(): Boolean {
        return isGameOver
    }

    fun resetGame() {
        isGameOver = false
        moveHistory.clear()
        canUndo.postValue(false)
        board = Array(15) { Array(15) { Piece.EMPTY } }
    }

    fun aiMove() {
        aiThinking = true
        viewModelScope.launch(Dispatchers.Default) {
            val move = ai.findBestMove(board, aiSymbol)
            if (move.x != null && move.y != null) {
                board[move.x!!][move.y!!] = aiSymbol
                val gameMove = GameMove(move.x!!, move.y!!, aiSymbol)
                mGameMove.postValue(gameMove)

                // 将本轮两个人的落子合并保存
                pendingHumanMove?.let { humanMove ->
                    moveHistory.push(MovePair(humanMove, gameMove))
                    pendingHumanMove = null // 清空暂存
                    if (!canUndo.value!!) {
                        canUndo.postValue(true)
                    }
                }
            }
            aiThinking = false
        }
    }

    fun humanMove(x: Int, y: Int) {
        board[x][y] = humanSymbol
        val move = GameMove(x, y, humanSymbol)
        mGameMove.postValue(move)
        pendingHumanMove = move // 暂存人类落子
    }

    fun undoLastMove() {

        if (moveHistory.isNotEmpty()) {
            val lastRound = moveHistory.pop()
            // 撤销 AI 落子
            board[lastRound.aiMove.x][lastRound.aiMove.y] = Piece.EMPTY
            mGameMove.postValue(GameMove(lastRound.aiMove.x, lastRound.aiMove.y, Piece.EMPTY))
            // 撤销 Human 落子
            board[lastRound.humanMove.x][lastRound.humanMove.y] = Piece.EMPTY
            mUndoGameMove.postValue(lastRound)
            canUndo.postValue(moveHistory.isNotEmpty())
            return
        }
        canUndo.postValue(false)
    }

    // 更新分数的方法
    fun updateScores(result: Piece) {
        when (result) {
            aiSymbol -> {
                aiWins++
            }
            humanSymbol -> {
                playerWins++
            }
            else -> {
                draw++
            }
        }
        scoreLiveData.postValue(Triple(aiWins, playerWins, draw))
    }

    fun clickValid(x: Int, y: Int): Boolean {
        return board[x][y] == Piece.EMPTY
    }

    /**
     * 判断当前落子是否连成五子
     */
    fun isTerminalNode(x: Int, y: Int): Piece {
        if (ai.isTerminalNode(board, GomokuAI.Cell(x, y))) {
            isGameOver = true
            return board[x][y]
        }
        return Piece.EMPTY
    }

    fun isGameBoardFull(): Boolean {
        for (i in 0 until 15) {
            for (j in 0 until 15) {
                if (board[i][j] == Piece.EMPTY) {
                    return false
                }
            }
        }
        return true
    }
}