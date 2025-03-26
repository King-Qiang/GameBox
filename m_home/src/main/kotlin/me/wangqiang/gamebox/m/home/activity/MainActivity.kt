package me.wangqiang.gamebox.m.home.activity

import me.wangqiang.c.common.mvvm.MVVMBaseActivity
import me.wangqiang.gamebox.m.home.BR
import me.wangqiang.gamebox.m.home.R
import me.wangqiang.gamebox.m.home.databinding.HomeActivityMainBinding
import me.wangqiang.gamebox.m.home.viewmodel.MainViewModel

class MainActivity : MVVMBaseActivity<MainViewModel, HomeActivityMainBinding>() {
    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutID(): Int {
        return R.layout.home_activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun initView() {
        super.initView()
    }

    override fun initObserver() {
        super.initObserver()
    }
}