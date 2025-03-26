package me.wangqiang.c.common.mvvm

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider

abstract class MVVMBaseActivity<VM : BaseViewModel, DB : ViewDataBinding>: AppCompatActivity() {
    lateinit var mViewModel: VM
    lateinit var mBinding: DB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(layoutInflater, getLayoutID(), null, false)
        mBinding.lifecycleOwner = this
        mViewModel = ViewModelProvider(this)[getViewModelClass()]
        // 关键点：自动绑定 ViewModel 到布局变量
        mBinding.setVariable(getBindingVariable(), mViewModel)
        mBinding.executePendingBindings() // 立即刷新绑定
        setContentView(mBinding.root)
        initView()
        initObserver()
    }

    open fun initView() {

    }

    open fun initObserver() {

    }

    abstract fun getBindingVariable(): Int
    @LayoutRes
    abstract fun getLayoutID(): Int
    abstract fun getViewModelClass(): Class<VM>
}