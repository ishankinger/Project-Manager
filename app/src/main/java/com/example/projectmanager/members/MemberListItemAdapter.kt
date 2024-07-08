package com.example.projectmanager.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemMembersBinding
import com.example.projectmanager.models.User

/**
 * Adapter to show the member list of the boards
 */
class MemberListItemAdapter(private val clickListener: MembersClickListener) : ListAdapter<User,
        MemberListItemAdapter.ViewHolder>(MembersDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemMembersBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(item: User, clickListener: MembersClickListener) {

            binding.memberName.text = item.name
            binding.memberEmailId.text = item.email

            Glide.with(binding.root)
                .load(item.image)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(binding.memberImage)

            if(item.selected){
                binding.ivSelectedMember.visibility = View.VISIBLE
            }
            else{
                binding.ivSelectedMember.visibility = View.GONE
            }

            binding.member = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMembersBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class MembersDiffCallBack : DiffUtil.ItemCallback<User>(){
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return  oldItem.id == newItem.id &&
                    oldItem.image == newItem.image
        }
    }

    class MembersClickListener(val clickListener: (member : User) -> Unit) {
        fun onClick(member: User) = clickListener(member)
    }
}
