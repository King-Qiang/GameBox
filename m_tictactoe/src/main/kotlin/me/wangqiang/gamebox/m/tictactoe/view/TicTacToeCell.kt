package me.wangqiang.gamebox.m.tictactoe.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import me.wangqiang.gamebox.m.tictactoe.R

class TicTacToeCell(context: Context, attrs: AttributeSet?, defStyleAttr: Int = R.style.BoardCellButton) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        setBackgroundResource(R.drawable.cell_background) // 使用之前定义的 cell_background.xml
    }
}
