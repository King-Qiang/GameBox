package me.wangqiang.gamebox.m.tzfe.bean

data class TileMovement(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val isMerge: Boolean = false
)
