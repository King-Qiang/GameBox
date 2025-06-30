package me.wangqiang.gamebox.m.gobang.util

import me.wangqiang.gamebox.m.gobang.bean.Board
import me.wangqiang.gamebox.m.gobang.bean.MCTSNode
import me.wangqiang.gamebox.m.gobang.bean.Piece
import me.wangqiang.gamebox.m.gobang.bean.Position

class MCTS(private val maxIterations: Int = 1000) {
    fun findBestMove(rootBoard: Board): Position {
        val rootNode = MCTSNode(rootBoard, null)

        repeat(maxIterations) {
            var node = rootNode
            val board = rootBoard.copy()

            // 1. 选择阶段：沿树向下选择子节点，直到未完全展开或叶子节点
            while (node.isFullyExpanded() && node.children.isNotEmpty()) {
                node = node.selectChild()
                board.placePiece(node.move!!, currentPlayer(board))
            }

            // 2. 扩展阶段：如果节点未被完全展开，创建新子节点
            if (!node.isFullyExpanded()) {
                val move = node.untriedMoves.random()
                node.untriedMoves -= move
                val newBoard = board.copy()
                newBoard.placePiece(move, currentPlayer(newBoard))
                val newNode = MCTSNode(newBoard, node, move)
                node.children.add(newNode)
                node = newNode
            }

            // 3. 模拟阶段：从当前节点随机模拟直到游戏结束
            val result = simulate(board.copy())

            // 4. 反向传播：更新路径上的节点统计信息
            backpropagate(node, result)
        }

        // 选择访问次数最多的子节点
        return rootNode.children.maxByOrNull { it.visits }!!.move!!
    }

    // 判断当前玩家（假设黑方先手）
    private fun currentPlayer(board: Board): Piece {
        val blackCount = board.grid.sumOf { row -> row.count { it == Piece.BLACK } }
        val whiteCount = board.grid.sumOf { row -> row.count { it == Piece.WHITE } }
        return if (blackCount == whiteCount) Piece.BLACK else Piece.WHITE
    }

    // 随机模拟对局
    private fun simulate(board: Board): Double {
        var currentPlayer = currentPlayer(board)
        while (true) {
            val legalMoves = board.getEmptyCells()
            if (legalMoves.isEmpty()) return 0.5 // 平局
            val move = legalMoves.random()
            board.placePiece(move, currentPlayer)
            if (board.checkWin(currentPlayer)) {
                return if (currentPlayer == Piece.BLACK) 1.0 else 0.0 // 黑胜1，白胜0
            }
            currentPlayer = if (currentPlayer == Piece.BLACK) Piece.WHITE else Piece.BLACK
        }
    }

    // 反向传播结果
    private fun backpropagate(node: MCTSNode?, result: Double) {
        var currentNode = node
        while (currentNode != null) {
            currentNode.visits++
            currentNode.totalValue += result
            currentNode = currentNode.parent
        }
    }
}