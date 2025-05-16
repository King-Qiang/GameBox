package me.wangqiang.gamebox.m.home.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import me.wangqiang.c.common.mvvm.MVVMBaseActivity
import me.wangqiang.gamebox.c.interfaces.path.RoutePath_TicTacToe
import me.wangqiang.gamebox.m.home.BR
import me.wangqiang.gamebox.m.home.R
import me.wangqiang.gamebox.m.home.adapter.GameMenuRecyclerAdapter
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
        mBinding.rvGameList.adapter = GameMenuRecyclerAdapter(mViewModel.getGameMenuList()).apply {
            setOnItemClickListener {
                ARouter.getInstance().build(it.path).navigation()
                Unit
            }
        }
        mBinding.rvGameList.layoutManager = GridLayoutManager(this, 4);

    }

    override fun initObserver() {
        super.initObserver()
    }
}