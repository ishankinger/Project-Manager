package com.example.projectmanager.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemCardSelectedMemberBinding
import com.example.projectmanager.models.SelectedMembers

/**
 * Adapter to show list of members of the boards assigned to a particular card
 */
class CardMembersListItemAdapter(private val listSize : Int,
                                 private val assignedMember : Boolean = false,
                                 private val clickListener: CardMembersClickListener)
    : ListAdapter<SelectedMembers, CardMembersListItemAdapter.ViewHolder>(CardMembersDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener,position,listSize,assignedMember)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemCardSelectedMemberBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(item: SelectedMembers, clickListener: CardMembersClickListener,
                 position: Int, listSize: Int, assignedMember: Boolean) {

            if(position == listSize-1 && assignedMember){
                binding.ivAddMember.visibility = View.VISIBLE
                binding.ivSelectedMemberImage.visibility = View.GONE
            }
            else{
                binding.ivAddMember.visibility = View.GONE
                binding.ivSelectedMemberImage.visibility = View.VISIBLE

                Glide
                    .with(binding.root)
                    .load(item.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivSelectedMemberImage)

            }
            binding.member = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCardSelectedMemberBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class CardMembersDiffCallBack : DiffUtil.ItemCallback<SelectedMembers>(){
        override fun areContentsTheSame(oldItem: SelectedMembers, newItem: SelectedMembers): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: SelectedMembers, newItem: SelectedMembers): Boolean {
            return  oldItem.id == newItem.id &&
                    oldItem.image == newItem.image
        }
    }

    class CardMembersClickListener(val clickListener: (member: SelectedMembers) -> Unit) {
        fun onClick(member: SelectedMembers) = clickListener(member)
    }
}
