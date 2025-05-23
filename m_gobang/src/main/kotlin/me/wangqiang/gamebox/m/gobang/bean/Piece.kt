package me.wangqiang.gamebox.m.gobang.bean

enum class Piece {
    EMPTY,
    BLACK,
    WHITE;

    fun opponent() = when (this) {
        EMPTY -> EMPTY
        BLACK -> WHITE
        WHITE -> BLACK
    }
}