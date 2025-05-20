package me.wangqiang.gamebox.m.tzfe.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.PointF
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import me.wangqiang.gamebox.m.tzfe.R
import me.wangqiang.gamebox.m.tzfe.bean.TileMovement

class TileAnimator(
    private val gridLayout: GridLayout,
    private val statusBarHeight: Int,
    private val cellPadding: Int,
    private val cellSize: Int
) {
    // 临时存储克隆视图
    private val tempViews = mutableMapOf<Int, View>()
    private var animatorFinish: ((Boolean) -> Unit)? = null

    fun setAnimatorFinish(animatorFinish: (Boolean) -> Unit) {
        this.animatorFinish = animatorFinish
    }

    // 执行所有动画
    fun executeAnimations(movements: List<TileMovement>) {
        val animators = mutableListOf<Animator>()

        // 处理移动动画
        movements.forEach { move ->
            animators.add(createMoveAnimator(move))
        }

        // 执行组合动画
        AnimatorSet().apply {
            playTogether(animators)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    cleanupTempViews()
                    animatorFinish?.invoke(true)
                }
            })
            duration = 220
            start()
        }
    }

    // 创建移动动画
    private fun createMoveAnimator(move: TileMovement): Animator {
        val srcView = getViewAt(move.fromRow, move.fromCol)
        val destPos = calculatePosition(move.toRow, move.toCol)

        // 创建克隆视图用于动画
        val cloneView = createCloneView(srcView)
        cloneView.visibility = View.VISIBLE

        // 使用 PropertyValuesHolder 设置多个属性动画
        Log.i("tzfe", "createMoveAnimator srcView.x: ${srcView.x}, srcView.y: ${srcView.y}")
        Log.i("tzfe", "createMoveAnimator destPos.x: ${destPos.x}, destPos.y: ${destPos.y}: ")
        Log.i("tzfe", "createMoveAnimator gridLayout.left: ${gridLayout.left}, gridLayout.top: ${gridLayout.top}")
        val translationX = PropertyValuesHolder.ofFloat("translationX", srcView.x + gridLayout.left, destPos.x)
        val translationY = PropertyValuesHolder.ofFloat("translationY", srcView.y + gridLayout.top - statusBarHeight, destPos.y)

        return ObjectAnimator.ofPropertyValuesHolder(cloneView, translationX, translationY).apply {
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    cloneView.visibility = View.GONE // 动画结束后隐藏克隆视图
                }

                override fun onAnimationStart(animation: Animator) {
                    srcView.setBackgroundResource(R.drawable.bg_number_defalt)
                    (srcView as TextView).text  = ""
                }
            })
        }
    }

    // 创建克隆视图
    private fun createCloneView(original: View): View {
        return TextView(original.context).apply {
            layoutParams = FrameLayout.LayoutParams(cellSize, cellSize).apply {
                leftMargin = original.x.toInt()
                topMargin = original.y.toInt()
            }
            text = (original as TextView).text
            textSize = original.textSize / resources.displayMetrics.scaledDensity
            setTextColor(original.currentTextColor)
            gravity = Gravity.CENTER
            background = original.background.constantState?.newDrawable()
            alpha = 0.8f
            // 添加到父容器
            (gridLayout.parent as ViewGroup).addView(this)
            tempViews[original.hashCode()] = this
        }
    }

    // 清理临时视图
    private fun cleanupTempViews() {
        tempViews.values.forEach { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        tempViews.clear()
    }

    // 获取指定位置的视图
    private fun getViewAt(row: Int, col: Int): View {
        return gridLayout.getChildAt(row * gridLayout.columnCount + col)
    }

    // 计算目标位置坐标
    private fun calculatePosition(row: Int, col: Int): PointF {
        return PointF(
            (col * cellSize + (col + 1) * cellPadding + gridLayout.left).toFloat(),
            (row * cellSize + (row + 1) * cellPadding + gridLayout.top - statusBarHeight).toFloat()
        )
    }
}