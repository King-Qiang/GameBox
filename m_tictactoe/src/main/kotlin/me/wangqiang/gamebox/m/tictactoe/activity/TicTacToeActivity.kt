package me.wangqiang.gamebox.m.tictactoe.activity

import android.widget.GridLayout
import com.alibaba.android.arouter.facade.annotation.Route
import me.wangqiang.c.common.mvvm.MVVMBaseActivity
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TicTacToe
import me.wangqiang.gamebox.m.tictactoe.BR
import me.wangqiang.gamebox.m.tictactoe.NoticeDialog
import me.wangqiang.gamebox.m.tictactoe.R
import me.wangqiang.gamebox.m.tictactoe.databinding.ActivityTicTacToeBinding
import me.wangqiang.gamebox.m.tictactoe.view.TicTacToeCell
import me.wangqiang.gamebox.m.tictactoe.viewmodel.TicTacToeViewModel

@Route(path = RoutePath_TicTacToe.Page.TicTacToe_Main)
class TicTacToeActivity : MVVMBaseActivity<TicTacToeViewModel, ActivityTicTacToeBinding>() {

    private val board = Array(3) { arrayOfNulls<TicTacToeCell>(3) }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_tic_tac_toe
    }

    override fun getViewModelClass(): Class<TicTacToeViewModel> {
        return TicTacToeViewModel::class.java
    }

    override fun initView() {
        // 初始化棋盘
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val ivCell = TicTacToeCell(this, null).apply {
                    setOnClickListener { onCellClicked(i, j) }
                }
                board[i][j] = ivCell
                mBinding.gridLayout.addView(ivCell, GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(j, 1f)
                    rowSpec = GridLayout.spec(i, 1f)
                    setMargins(8, 8, 8, 8)
                })
            }
        }
    }

    override fun initObserver() {
        mViewModel.mGameMove.observe(this) {
            val ivCell = board[it.x][it.y]
            ivCell!!.setImageResource(if (it.playerType == TicTacToeViewModel.PLAYER_X) R.drawable.ic_x else R.drawable.ic_o)
            when (val gameResult = mViewModel.checkGameResult()) {
                TicTacToeViewModel.DRAW -> {
                    mBinding.statusTextView.text = getString(R.string.draw_notice)
                    mViewModel.updateScores(gameResult)
                    showNoticeDialog(getString(R.string.draw_notice))
                    return@observe
                }
                TicTacToeViewModel.PLAYER_X -> {
                    mBinding.statusTextView.text = getString(R.string.win_notice)
                    mViewModel.updateScores(gameResult)
                    showNoticeDialog(getString(R.string.win_notice))
                    return@observe
                }
                TicTacToeViewModel.PLAYER_O -> {
                    mBinding.statusTextView.text = getString(R.string.lose_notice)
                    mViewModel.updateScores(gameResult)
                    showNoticeDialog(getString(R.string.lose_notice))
                    return@observe
                }
                else -> {
                    mBinding.statusTextView.text =
                        if (it.playerType == TicTacToeViewModel.PLAYER_X) "It is AI turn" else "It is your turn"
                    if (it.playerType == TicTacToeViewModel.PLAYER_X) mViewModel.aiMove()
                }
            }
        }
        mBinding.btnExit.setOnClickListener{
            finish()
        }
        mBinding.btnRestart.setOnClickListener{
            restartGame()
        }
    }

    private fun restartGame() {
        mViewModel.resetGame()
        mBinding.statusTextView.text = "It is your turn"
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] != null) {
                    board[i][j]!!.setImageResource(0)
                }
            }
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        if (mViewModel.checkGameResult() == TicTacToeViewModel.EMPTY && mViewModel.clickValid(row, col)) {
            mViewModel.humanMove(row, col)
        }
    }

    private fun showNoticeDialog(title: String) {
        NoticeDialog.Builder(this, null)
            .setTitle(title)
            .setContent("Do you want to play again?")
            .setBtnUpText("Yes"){
                restartGame()
            }
            .setBtnDownText("Exit"){
                finish()
            }
            .build()
            .show()
    }
}