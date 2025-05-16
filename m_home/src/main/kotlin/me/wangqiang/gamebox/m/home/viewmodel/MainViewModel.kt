package me.wangqiang.gamebox.m.home.viewmodel

import android.app.Application
import me.wangqiang.c.common.mvvm.BaseViewModel
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TicTacToe
import me.wangqiang.gamebox.m.home.GameMenuData
import me.wangqiang.gamebox.m.home.R

class MainViewModel(application: Application) : BaseViewModel(application) {

    fun getGameMenuList(): List<GameMenuData> {
        return listOf(
            GameMenuData("井字棋", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
            GameMenuData("2048", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
            GameMenuData("五子棋", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
            GameMenuData("象棋", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
            GameMenuData("围棋", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
        )
    }
}