package me.wangqiang.gamebox.m.home.adapter

import me.wangqiang.c.common.mvvm.BaseRecyclerAdapter
import me.wangqiang.gamebox.m.home.BR
import me.wangqiang.gamebox.m.home.R
import me.wangqiang.gamebox.m.home.databinding.ItemGameMenuBinding

class GameMenuRecyclerAdapter(private var itemList: List<String>): BaseRecyclerAdapter<ItemGameMenuBinding, String>(itemList){
    override fun bindViewHolder(
        viewHolder: BindingRecyclerViewHolder<ItemGameMenuBinding>,
        item: String
    ) {

    }

    override fun getBindingItem(): Int {
        return BR.bindItem
    }

    override fun getLayoutID(): Int {
        return R.layout.item_game_menu
    }
}