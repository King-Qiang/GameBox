package me.wangqiang.gamebox.m.gobang.bean

import kotlin.math.ln
import kotlin.math.sqrt

class MCTSNode(
    val board: Board,          // 当前棋盘状态
    val parent: MCTSNode?,     // 父节点
    val move: Position? = null // 到达此节点的移动
) {
    var visits = 0             // 访问次数
    var totalValue = 0.0       // 累计奖励值
    val children = mutableListOf<MCTSNode>() // 子节点
    var untriedMoves: List<Position> = board.getEmptyCells() // 未尝试的移动

    // 是否完全展开
    fun isFullyExpanded(): Boolean = untriedMoves.isEmpty()

    // 选择最佳子节点（UCT算法）
    fun selectChild(explorationWeight: Double = 1.414): MCTSNode {
        return children.maxByOrNull {
            it.totalValue / it.visits + explorationWeight *
                    sqrt(ln(visits.toDouble()) / it.visits)
        }!!
    }
}