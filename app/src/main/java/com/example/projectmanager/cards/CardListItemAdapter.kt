package com.example.projectmanager.cards

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.databinding.ItemCardsBinding
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.SelectedMembers
import com.example.projectmanager.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter to show the list of the cards in the task lists
 */
class CardListItemAdapter(private val context: Context,
                          private val mMembersDetailList : ArrayList<User>,
                          private val clickListener: CardsClickListener
                        ) : ListAdapter<Card, CardListItemAdapter.ViewHolder>(CardDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener,context,mMembersDetailList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemCardsBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(
            item: Card,
            clickListener: CardsClickListener,
            context: Context,
            mMembersDetailList: ArrayList<User>
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

            if(mMembersDetailList.size > 0){
                val cardAssignedMembersList = item.assignedTo

                val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

                for(i in mMembersDetailList.indices){
                    for(j in cardAssignedMembersList){
                        if(mMembersDetailList[i].id == j){
                            val selectedMember = SelectedMembers(
                                mMembersDetailList[i].id,
                                mMembersDetailList[i].image
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                /**
                 * Connecting the selected member list in the cards of task List
                 */
                if(selectedMembersList.size > 0){
                    binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                    binding.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context,5)

                    val cardMemberListItemAdapter = CardMembersListItemAdapter(selectedMembersList.size,false,
                        CardMembersListItemAdapter.CardMembersClickListener {
                        Toast.makeText(context,"Members of Card",Toast.LENGTH_SHORT).show()
                    })

                    binding.rvCardSelectedMembersList.adapter = cardMemberListItemAdapter
                    cardMemberListItemAdapter.submitList(selectedMembersList)
                }
            }
            binding.tvCardName.text = item.name
            binding.card = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCardsBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class CardDiffCallBack : DiffUtil.ItemCallback<Card>(){
        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return  oldItem.name == newItem.name &&
                    oldItem.createdBy == newItem.createdBy
        }
    }

    class CardsClickListener(val clickListener: (card: Card) -> Unit) {
        fun onClick(card: Card) = clickListener(card)
    }
}
