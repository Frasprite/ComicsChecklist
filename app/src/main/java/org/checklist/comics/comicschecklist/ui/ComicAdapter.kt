package org.checklist.comics.comicschecklist.ui

import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.databinding.ListItemViewBinding

internal class ComicAdapter internal constructor(private val mComicClickCallback: ComicClickCallback?) : RecyclerView.Adapter<ComicAdapter.ComicViewHolder>() {

    internal lateinit var mComicList: List<ComicEntity>

    fun setComicList(comicList: List<ComicEntity>) {
        if (!::mComicList.isInitialized) {
            mComicList = comicList
            notifyItemRangeInserted(0, comicList.size)
        } else {
            val result = DiffUtil.calculateDiff(ComicDiffCallback(comicList, mComicList))
            mComicList = comicList
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val binding = DataBindingUtil
                .inflate<ListItemViewBinding>(LayoutInflater.from(parent.context), R.layout.list_item_view,
                        parent, false)
        binding.callback = mComicClickCallback
        return ComicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        holder.binding.comic = mComicList[position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mComicList.size
    }

    internal class ComicViewHolder(val binding: ListItemViewBinding) : RecyclerView.ViewHolder(binding.root)
}
