package me.wangqiang.gamebox.m.gobang.view

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.wangqiang.gamebox.m.gobang.bean.GameMove
import me.wangqiang.gamebox.m.gobang.bean.MovePair
import me.wangqiang.gamebox.m.gobang.bean.Piece
import kotlin.math.E

class GameView(context: Context, attrs: AttributeSet ) : View(context, attrs)  {
    private val boardSize = 15
    private var board = Array(boardSize) { Array(boardSize) { Piece.EMPTY } }
    private val cellSize: Float by lazy {
        (resources.displayMetrics.widthPixels - 2 * padding) / (15 - 1).toFloat()
    }
    private val padding = resources.getDimensionPixelSize(me.wangqiang.gamebox.c.common.R.dimen.c_ui_dimen_16)
    private val stoneRadius = cellSize / 2f - 5f
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private var boardBitmap: Bitmap? = null
    private val boardPaint = Paint()

    init {
//        setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN && !game.isGameOver) {
//                val x = Math.round((event.x - padding) / cellSize)
//                val y = Math.round((event.y - padding) / cellSize)
//                if (x in 0 until game.boardSize && y in 0 until game.boardSize) {
//                    if (game.placePiece(y, x)) {
//                        invalidate()
//                    }
//                }
//                performClick() // 添加此行以满足无障碍点击要求
//            }
//            true
//        }
    }

    override fun onDraw(canvas: Canvas) {
        if (boardBitmap == null) {
            boardBitmap = createBoardBitmap()
        }
        canvas.drawBitmap(boardBitmap!!, 0f, 0f, null)
        drawStones(canvas)
    }

    private fun createBoardBitmap(): Bitmap {
        val size = width
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 绘制背景
        boardPaint.color = Color.parseColor("#FFD699")
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), boardPaint)

        // 绘制棋盘线
        boardPaint.color = Color.BLACK
        for (i in 0 until boardSize) {
            val start = padding + i * cellSize
            canvas.drawLine(padding.toFloat(), start, (width - padding).toFloat(), start, boardPaint) // 横线
            canvas.drawLine(start, padding.toFloat(), start, (height - padding).toFloat(), boardPaint) // 竖线
        }

        // 绘制星位点（以 15x15 棋盘为例）
        if (boardSize == 15) {
            val starPoints = listOf(
                Pair(3, 3),
                Pair(3, 11),
                Pair(7, 7),
                Pair(11, 3),
                Pair(11, 11)
            )

            boardPaint.color = Color.BLACK
            val dotRadius = 6f // 加粗点的半径

            for ((row, col) in starPoints) {
                val cx = padding + col * cellSize
                val cy = padding + row * cellSize
                canvas.drawCircle(cx, cy, dotRadius, boardPaint)
            }
        }
        return bitmap
    }

    private fun drawStones(canvas: Canvas) {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val stone = board[i][j]
                if (stone != Piece.EMPTY) {
                    val cx = padding + j * cellSize
                    val cy = padding + i * cellSize
                    paint.color = if (stone == Piece.BLACK) Color.BLACK else Color.WHITE
                    canvas.drawCircle(cx, cy, stoneRadius, paint)

                    // 绘制白棋边框
                    if (stone == Piece.WHITE) {
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f
                        paint.color = Color.BLACK
                        canvas.drawCircle(cx, cy, stoneRadius, paint)
                        paint.style = Paint.Style.FILL
                    }
                }
            }
        }
    }

    fun updateBoard(move: GameMove) {
        board[move.x][move.y] = move.playerType
        invalidate()
    }

    fun updateBoard(lastMove: MovePair) {
        board[lastMove.humanMove.x][lastMove.humanMove.y] = Piece.EMPTY
        board[lastMove.aiMove.x][lastMove.aiMove.y] = Piece.EMPTY
        invalidate()
    }

    fun reset() {
        board = Array(boardSize) { Array(boardSize) { Piece.EMPTY } }
        invalidate()
    }

    fun setOnClickPieceListener(listener: (Int, Int) -> Unit) {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN ) {
                val x = Math.round((event.x - padding) / cellSize)
                val y = Math.round((event.y - padding) / cellSize)
                listener.invoke(y, x)
                performClick() // 添加此行以满足无障碍点击要求
            }
            true
        }
    }
}