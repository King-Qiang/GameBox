package me.wangqiang.gamebox.m.gobang.bean

import me.wangqiang.gamebox.m.gobang.util.GomokuAI.Cell

// 定义棋盘和棋子类型
const val BOARD_SIZE = 15 // 15x15 棋盘

data class Position(val x: Int, val y: Int)

class Board {
    val grid = Array(BOARD_SIZE) { Array(BOARD_SIZE) { Piece.EMPTY } }
    var lastMove: Position? = null // 记录最后一步，用于优化胜负判断

    // 检查位置是否合法
    fun isValidPosition(pos: Position): Boolean {
        return pos.x in 0 until BOARD_SIZE && pos.y in 0 until BOARD_SIZE
    }

    // 落子
    fun placePiece(pos: Position, piece: Piece): Boolean {
        if (!isValidPosition(pos) || grid[pos.x][pos.y] != Piece.EMPTY) return false
        grid[pos.x][pos.y] = piece
        lastMove = pos
        return true
    }

    // 判断是否胜利（基于最后一步优化）
    fun checkWin(piece: Piece): Boolean {
        val last = lastMove ?: return false
        val directions = listOf(
            Pair(1, 0),  // 水平
            Pair(0, 1),  // 垂直
            Pair(1, 1),  // 正对角线
            Pair(1, -1)  // 反对角线
        )

        for ((dx, dy) in directions) {
            var count = 1
            // 正向检查
            var x = last.x + dx
            var y = last.y + dy
            while (isValidPosition(Position(x, y)) && grid[x][y] == piece) {
                count++
                x += dx
                y += dy
            }
            // 反向检查
            x = last.x - dx
            y = last.y - dy
            while (isValidPosition(Position(x, y)) && grid[x][y] == piece) {
                count++
                x -= dx
                y -= dy
            }
            if (count >= 5) return true
        }
        return false
    }

    // 生成所有合法移动
    fun getLegalMoves(): List<Position> {
        val moves = mutableListOf<Position>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (grid[i][j] == Piece.EMPTY) {
                    moves.add(Position(i, j))
                }
            }
        }
        return moves
    }

    // 优化后的候选位置生成（只搜索有棋子的周围位置）
    fun getEmptyCells(): List<Position> {
        val moves = mutableListOf<Position>()
        val radius = 1 // 搜索半径

        for (i in grid.indices) {
            for (j in grid[i].indices) {
                if (grid[i][j] != Piece.EMPTY) {
                    for (dx in -radius..radius) {
                        for (dy in -radius..radius) {
                            val x = i + dx
                            val y = j + dy
                            if (x in grid.indices && y in grid[x].indices && grid[x][y] == Piece.EMPTY) {
                                moves.add(Position(x, y))
                            }
                        }
                    }
                }
            }
        }
        return moves.toList()
    }

    // 复制棋盘
    fun copy(): Board {
        val newBoard = Board()
        for (i in 0 until BOARD_SIZE) {
            newBoard.grid[i] = grid[i].copyOf()
        }
        newBoard.lastMove = lastMove
        return newBoard
    }
}