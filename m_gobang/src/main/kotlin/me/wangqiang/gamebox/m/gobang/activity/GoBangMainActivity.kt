package me.wangqiang.gamebox.m.gobang.activity

import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Route
import me.wangqiang.c.common.mvvm.MVVMBaseActivity
import me.wangqiang.c.common.mvvm.dialog.NoticeDialog
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_GoBang
import me.wangqiang.gamebox.m.gobang.BR
import me.wangqiang.gamebox.m.gobang.R
import me.wangqiang.gamebox.m.gobang.bean.Piece
import me.wangqiang.gamebox.m.gobang.databinding.GobangActivityMainBinding
import me.wangqiang.gamebox.m.gobang.viewmodel.GoBangMainViewModel

@Route(path = RoutePath_GoBang.Page.GoBang_Main)
class GoBangMainActivity: MVVMBaseActivity<GoBangMainViewModel, GobangActivityMainBinding>() {
    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutID(): Int {
        return R.layout.gobang_activity_main
    }

    override fun getViewModelClass(): Class<GoBangMainViewModel> {
        return GoBangMainViewModel::class.java
    }

    override fun initView() {
    }

    override fun initObserver() {
        mBinding.gameBoard.setOnClickPieceListener { x, y ->
            Log.i("gobang", "initObserver x: $x, y: $y")
            if (mViewModel.isGameOver()) {
                showResultDialog(getString(R.string.game_over), getString(R.string.play_again))
                return@setOnClickPieceListener
            }
            if (mViewModel.isAiThinking()) {
                showNoticeDialog(getString(R.string.ai_thinking))
                return@setOnClickPieceListener
            }
            if (mViewModel.clickValid(x, y)) {
                mViewModel.humanMove(x, y)
            }
        }
        mViewModel.mGameMove.observe(this) {
            Log.i("gobang", "updateBoard x: ${it.x}, y: ${it.y}: ")
            mBinding.gameBoard.updateBoard(it)
            val piece = mViewModel.isTerminalNode(it.x, it.y)
            if (piece == Piece.BLACK) {
                mViewModel.updateScores(Piece.BLACK)
                showResultDialog(getString(R.string.lose_notice), getString(R.string.play_again))
            } else if (piece == Piece.WHITE) {
                mViewModel.updateScores(Piece.WHITE)
                showResultDialog(getString(R.string.win_notice), getString(R.string.play_again))
            } else if (piece == Piece.EMPTY){
                if (mViewModel.isGameBoardFull()) {
                    mViewModel.updateScores(Piece.EMPTY)
                    showResultDialog(getString(R.string.draw_notice), getString(R.string.play_again))
                    return@observe
                }
                if (it.playerType == Piece.WHITE) {
                    mViewModel.aiMove()
                }
            }
        }
        mViewModel.mUndoGameMove.observe(this) {
            Log.i("gobang", "updateBoard hold")
            mBinding.gameBoard.updateBoard(it)
        }
        mViewModel.canUndo.observe(this) {
            mBinding.btnUndo.isEnabled = it
        }
        mBinding.btnExit.setOnClickListener {
            finish()
        }
        mBinding.btnRestart.setOnClickListener {
            showStartOverDialog()
        }
        mBinding.btnUndo.setOnClickListener {
            if (mViewModel.isGameOver()) {
                showResultDialog(getString(R.string.game_over), getString(R.string.play_again))
                return@setOnClickListener
            }
            mViewModel.undoLastMove()
        }
    }

    private fun showNoticeDialog(content: String) {
        NoticeDialog.Builder(this, null)
            .setTitle(getString(R.string.notice))
            .setContent(content)
            .setBtnUpText(getString(R.string.yes)) {
            }
            .build()
            .show()
    }

    private fun showResultDialog(title: String, content: String) {
        NoticeDialog.Builder(this, null)
            .setTitle(title)
            .setContent(content)
            .setBtnUpText(getString(R.string.yes)) {
                mBinding.gameBoard.reset()
                mViewModel.resetGame()
            }
            .setBtnDownText(getString(R.string.cancel)) {
            }
            .build()
            .show()
    }

    private fun showStartOverDialog() {
        NoticeDialog.Builder(this, null)
            .setTitle(getString(R.string.notice))
            .setContent(getString(R.string.play_start_over))
            .setBtnUpText(getString(R.string.start_over)) {
                mBinding.gameBoard.reset()
                mViewModel.resetGame()
            }
            .setBtnDownText(getString(R.string.cancel)) {
            }
            .build()
            .show()
    }
}