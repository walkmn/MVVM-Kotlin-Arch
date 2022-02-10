package com.blank_paper.lib.mvvm_arch

import androidx.databinding.ViewDataBinding
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableBoolean
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import com.blank_paper.lib.mvvm_arch.BR

open class BaseMVVMAdapter<T, B : ViewDataBinding?>(private val itemLayout: Int, listener: Listener<T>? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val itemCountObs = ObservableInt(0)
    val isEmpty = ObservableBoolean(true)
    val isLoading = ObservableBoolean(false)

    private var listener: Listener<T>? = null
    private val items: MutableList<T> = ArrayList()

    fun invokeClick(position: Int) {
        if (position in 0 until itemCount) {
            if (listener != null) {
                listener!!.onItemClicked(getItems()[position])
            }
        }
    }

    interface Listener<T> {
        fun onItemClicked(item: T)
    }

    fun setListener(listener: Listener<T>?) {
        this.listener = listener
    }

    fun clearItems() {
        setItems(ArrayList())
    }

    fun setItems(items: List<T>?) {
        if (items == null) return
        val dffUtilsCallback = UniversalDiffUtils(getItems(), items)
        val diffResult = DiffUtil.calculateDiff(dffUtilsCallback)
        this.items.clear()
        this.items.addAll(items)

        itemCountObs.set(itemCount)
        isEmpty.set(itemCount == 0)

        diffResult.dispatchUpdatesTo(this)
    }

    fun getItems() =  items

    fun getLastItem() = items[items.size - 1]

    protected val bindingVariable: Int
        get() = BR.viewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ViewHolder<B>(DataBindingUtil.inflate(LayoutInflater.from(parent.context), itemLayout, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val viewHolder = holder as ViewHolder<*>
        viewHolder.binding!!.setVariable(bindingVariable, item)
        if (listener != null) {
            viewHolder.binding.root.setOnClickListener { listener!!.onItemClicked(item) }
            viewHolder.binding.setVariable(BR.listener, listener)
        }
        onBind(item, viewHolder.binding as B)
    }

    protected fun onBind(item: T, view: B) {}

    override fun getItemCount(): Int {
        return items.size
    }

    private class ViewHolder<B : ViewDataBinding?>(val binding: B) : RecyclerView.ViewHolder(binding!!.root)
}