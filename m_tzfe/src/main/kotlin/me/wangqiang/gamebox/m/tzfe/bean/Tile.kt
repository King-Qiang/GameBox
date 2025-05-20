package me.wangqiang.gamebox.m.tzfe.bean

data class Tile(var value: Int = 0, var mergedFrom: Pair<Tile, Tile>? = null) {
}