package com.example.projectmanager.cards

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.databinding.ItemUserCardsBinding
import com.example.projectmanager.models.Card
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UserCardListItemAdapter(private val clickListener: UserCardClickListener) : ListAdapter<Card,
        UserCardListItemAdapter.ViewHolder>(UserCardDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemUserCardsBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(
            item: Card,
            clickListener: UserCardClickListener
        ) {

            if(item.labelColor.isNotEmpty()){
                binding.viewLabelColor.visibility = View.VISIBLE
                binding.viewLabelColor.setBackgroundColor(Color.parseColor(item.labelColor))
            }
            else{
                binding.viewLabelColor.visibility = View.GONE
            }

            if(item.dueDate != 0L){
                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val selectedDate = simpleDateFormat.format(Date(item.dueDate))
                binding.tvDueDate.text = selectedDate
            }

            binding.tvCardName.text = item.name
            binding.card = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemUserCardsBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class UserCardDiffCallBack : DiffUtil.ItemCallback<Card>(){
        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return  oldItem.name == newItem.name &&
                    oldItem.createdBy == newItem.createdBy
        }
    }

    class UserCardClickListener(val clickListener: (card: Card) -> Unit) {
        fun onClick(card: Card) = clickListener(card)
    }
}
