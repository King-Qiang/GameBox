package me.wangqiang.gamebox.m.tzfe.activity

import android.os.Build
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.forEachIndexed
import com.alibaba.android.arouter.facade.annotation.Route
import me.wangqiang.c.common.mvvm.MVVMBaseActivity
import me.wangqiang.c.common.mvvm.dialog.NoticeDialog
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TZFE
import me.wangqiang.gamebox.m.tzfe.BR
import me.wangqiang.gamebox.m.tzfe.R
import me.wangqiang.gamebox.m.tzfe.bean.Direction
import me.wangqiang.gamebox.m.tzfe.bean.Tile
import me.wangqiang.gamebox.m.tzfe.databinding.ActivityMainBinding
import me.wangqiang.gamebox.m.tzfe.util.TileAnimator
import me.wangqiang.gamebox.m.tzfe.viewmodel.GameUiState
import me.wangqiang.gamebox.m.tzfe.viewmodel.TZFEMainViewModel
import kotlin.math.abs


@Route(path = RoutePath_TZFE.Page.TZFE_Main)
class TZFEMainActivity : MVVMBaseActivity<TZFEMainViewModel, ActivityMainBinding>() {
    private val swipeThreshold = 50 // 扩展函数转换dp为px
    private lateinit var tileAnimator: TileAnimator
    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<TZFEMainViewModel> {
        return TZFEMainViewModel::class.java
    }

    override fun initView() {
        val padding = resources.getDimensionPixelSize(me.wangqiang.gamebox.c.common.R.dimen.c_ui_dimen_8)
        val sPadding = resources.getDimensionPixelSize(me.wangqiang.gamebox.c.common.R.dimen.c_ui_dimen_4)
        mBinding.gridlayout.post {
            tileAnimator = TileAnimator(mBinding.gridlayout, getStatusBarHeight(), padding, calculateCellSize())
            tileAnimator.setAnimatorFinish {
                val gameGrid = mViewModel.getGrid()
                updateGrid(gameGrid)
            }
        }
        for (i in 0..3) {
            for (j in 0..3) {
                val textView = TextView(this).apply {
                    gravity = Gravity.CENTER
                    textSize = 20f
                    setBackgroundResource(getTileBackground(0))
                }
                mBinding.gridlayout.addView(textView, GridLayout.LayoutParams().apply {
                    width = calculateCellSize()
                    height = calculateCellSize()
                    when {
                        i == 0 && j == 0 -> setMargins(padding, padding, sPadding, sPadding)
                        i == 0 && j == 3 -> setMargins(sPadding, padding, padding, sPadding)
                        i == 3 && j == 0 -> setMargins(padding, sPadding, sPadding, padding)
                        i == 3 && j == 3 -> setMargins(sPadding, sPadding, padding, padding)

                        i == 0 -> setMargins(sPadding, padding, sPadding, sPadding)
                        i == 3 -> setMargins(sPadding, sPadding, sPadding, padding)
                        j == 0 -> setMargins(padding, sPadding, sPadding, sPadding)
                        j == 3 -> setMargins(sPadding, sPadding, padding, sPadding)

                        else -> setMargins(sPadding, sPadding, sPadding, sPadding)
                    }
                })
            }
        }
        setupControls()
    }

    override fun initObserver() {
        mViewModel.gameGrid.observe(this) {
            updateGrid(it)
        }
        mViewModel.movementsLiveData.observe(this) {
            tileAnimator.executeAnimations(it)
        }
        mViewModel.gameUiState.observe(this) {
            when (it) {
                is GameUiState.Playing -> {

                }
                is GameUiState.Win -> {
                    showNoticeDialog(getString(R.string.tzfe_finish_game, it.score.toString()), getString(R.string.play_again))
                }
                is GameUiState.GameOver -> {
                    showNoticeDialog(getString(R.string.tzfe_final_score_content, it.score.toString()), getString(R.string.play_again))
                }
            }
        }
        mBinding.btnExit.setOnClickListener {
            finish()
        }
        mBinding.btnRestart.setOnClickListener {
            showNoticeDialog(getString(R.string.notice), getString(R.string.play_start_over))
        }
    }

    // 手势检测优化
    private fun setupControls() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                return when {
                    abs(dx) > abs(dy) && abs(dx) > swipeThreshold -> {
                        mViewModel.handleMove(if (dx > 0) Direction.RIGHT else Direction.LEFT)
                        true
                    }
                    abs(dy) > abs(dx) && abs(dy) > swipeThreshold -> {
                        mViewModel.handleMove(if (dy > 0) Direction.DOWN else Direction.UP)
                        true
                    }
                    else -> false
                }
            }
        })

        mBinding.gridlayout.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun updateGrid(grid: Array<Array<Tile>>) {
        mBinding.gridlayout.apply {
            forEachIndexed { index, view ->
                val i = index / 4
                val j = index % 4
                val tile = grid[i][j]
                (view as TextView).apply {
                    text = if (tile.value == 0) {
                        ""
                    } else {
                        tile.value.toString()
                    }
                    setBackgroundResource(getTileBackground(tile.value))
                    animate().apply {
                        if (tile.mergedFrom != null) {
                            scaleX(1.1f).scaleY(1.1f).setDuration(100)
                                .withEndAction { scaleX(1f).scaleY(1f) }
                        }
                    }.start()
                }
            }
        }
    }

    private fun showNoticeDialog(title: String, content: String) {
        NoticeDialog.Builder(this, null)
            .setTitle(title)
            .setContent(content)
            .setBtnUpText(getString(R.string.yes)){
                mViewModel.reset()
            }
            .setBtnDownText(getString(R.string.cancel)){
            }
            .build()
            .show()
    }

    private fun getTileBackground(value: Int): Int {
        return when (value) {
            2 -> R.drawable.bg_number_2
            4 -> R.drawable.bg_number_4
            8 -> R.drawable.bg_number_8
            16 -> R.drawable.bg_number_16
            32 -> R.drawable.bg_number_32
            64 -> R.drawable.bg_number_64
            128 -> R.drawable.bg_number_128
            256 -> R.drawable.bg_number_256
            512 -> R.drawable.bg_number_512
            1024 -> R.drawable.bg_number_1024
            2048 -> R.drawable.bg_number_2048
            else -> R.drawable.bg_number_defalt
        }
    }

    private fun getStatusBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = windowManager?.currentWindowMetrics?.windowInsets
                ?: return 0
            val insets = windowInsets.getInsets(WindowInsets.Type.statusBars())
            insets.top
        } else {
            @Suppress("DEPRECATION")
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
    }


    private fun calculateCellSize(): Int {
        // 根据屏幕尺寸计算单元格大小
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels - resources.getDimensionPixelSize(me.wangqiang.gamebox.c.common.R.dimen.c_ui_dimen_104)
        return screenWidth / 4
    }

}