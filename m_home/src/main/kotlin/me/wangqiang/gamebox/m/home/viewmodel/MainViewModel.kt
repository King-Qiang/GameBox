package me.wangqiang.gamebox.m.home.viewmodel

import android.app.Application
import me.wangqiang.c.common.mvvm.BaseViewModel
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_FlappyBird
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_GoBang
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TZFE
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TicTacToe
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_Snake
import me.wangqiang.gamebox.m.home.GameMenuData
import me.wangqiang.gamebox.m.home.R

class MainViewModel(application: Application) : BaseViewModel(application) {

    fun getGameMenuList(): List<GameMenuData> {
        return listOf(
            GameMenuData("井字棋", R.drawable.ic_tic_tac_toe, RoutePath_TicTacToe.Page.TicTacToe_Main),
            GameMenuData("2048", R.drawable.ic_tzfe, RoutePath_TZFE.Page.TZFE_Main),
            GameMenuData("五子棋", R.drawable.ic_gobang, RoutePath_GoBang.Page.GoBang_Main),
            GameMenuData("像素鸟", R.drawable.ic_flappy_bird, RoutePath_FlappyBird.Page.FlappyBird_Main),
            GameMenuData("贪吃蛇", R.drawable.ic_snake, RoutePath_Snake.Page.Snake_Main)
        )
    }
}