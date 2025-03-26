package me.wangqiang.c.common.mvvm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<T: ViewDataBinding, R: Any>(private var itemList: List<R>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onItemClick: ((R) -> Unit?)? = null
    // 接收比较逻辑的函数参数
    private var areItemsSame: (oldItem: R, newItem: R) -> Boolean = { old, new -> old == new }
    private var areContentsSame: (oldItem: R, newItem: R) -> Boolean = { old, new -> old == new }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            getLayoutID(),
            parent,
            false
        )
        return BindingRecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as BindingRecyclerViewHolder<T>
        vh.binding.setVariable(getBindingItem(), itemList[position])
        vh.binding.executePendingBindings()
        vh.binding.root.setOnClickListener {
            onItemClick?.invoke(itemList[position])
        }
        bindViewHolder(vh, itemList[position])
    }

    fun notifyAll(itemList: List<R>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    fun notifyItem(position: Int, itemList: List<R>) {
        if (position < itemList.size) {
            notifyItemChanged(position)
        }
    }

    fun submitList(newItems: List<R>) {
        val diffCallback = BookDiffCallback(
            this.itemList,
            newItems,
            areItemsSame,
            areContentsSame
        )
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.itemList = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnItemClickListener(onItemClick: ((R) -> Unit?)?) {
        this.onItemClick = onItemClick
    }

    abstract fun bindViewHolder(viewHolder: BindingRecyclerViewHolder<T>, item: R)

    abstract fun getBindingItem(): Int

    abstract fun getLayoutID(): Int

    fun setAreSame(areItemsSame: (oldItem: R, newItem: R) -> Boolean, areContentsSame: (oldItem: R, newItem: R) -> Boolean) {
        this.areItemsSame = areItemsSame
        this.areContentsSame = areContentsSame
    }

    open class BindingRecyclerViewHolder<T: ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)

    // 定义 DiffCallback（需根据业务定制）
    private inner class BookDiffCallback(
        private val oldList: List<R>,
        private val newList: List<R>,
        private val areItemsSame: (oldItem: R, newItem: R) -> Boolean, // 判断是否是同一项
        private val areContentsSame: (oldItem: R, newItem: R) -> Boolean // 判断内容是否相同
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return areItemsSame.invoke(oldList[oldPos], newList[newPos]) // 根据业务调整（如比较唯一ID）
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return areContentsSame.invoke(oldList[oldPos], newList[newPos]) // 使用数据类的 equals()
        }
    }

}