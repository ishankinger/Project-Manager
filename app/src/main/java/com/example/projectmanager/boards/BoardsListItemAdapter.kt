package com.example.projectmanager.boards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemBoardsBinding
import com.example.projectmanager.models.Board

/**
 * This is the adapter for the boards list recycler view
 */

class BoardsListItemAdapter(private val clickListener: BoardsClickListener) : ListAdapter<Board,
        BoardsListItemAdapter.ViewHolder>(BoardsDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemBoardsBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(item: Board, clickListener: BoardsClickListener) {
            binding.boardName.text = item.name
            binding.boardCreatedBy.text = "Created By : " + item.createdBy
            Glide.with(binding.root)
                .load(item.image)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.color_gradient)
                .into(binding.boardImage)
            binding.board = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemBoardsBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class BoardsDiffCallBack : DiffUtil.ItemCallback<Board>(){
        override fun areContentsTheSame(oldItem: Board, newItem: Board): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Board, newItem: Board): Boolean {
            return  oldItem.name == newItem.name &&
                    oldItem.createdBy == newItem.createdBy
        }
    }

    class BoardsClickListener(val clickListener: (board: Board) -> Unit) {
        fun onClick(board: Board) = clickListener(board)
    }
}
