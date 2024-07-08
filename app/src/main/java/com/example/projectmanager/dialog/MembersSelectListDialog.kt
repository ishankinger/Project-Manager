package com.example.projectmanager.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.databinding.DialogListBinding
import com.example.projectmanager.members.MemberListItemAdapter
import com.example.projectmanager.models.User

/**
 * Members List dialog class which set up the recycler view and also select/unselect the members
 */
abstract class MembersSelectListDialog(
    context: Context?,
    private var list: ArrayList<User>,
    private val title: String = "")
        : Dialog(context!!){

        override fun onCreate(savedInstanceState : Bundle?){
            super.onCreate(savedInstanceState)

            val layoutInflater = LayoutInflater.from(context)
            val binding = DialogListBinding.inflate(layoutInflater,null,false)
            setContentView(binding.root)
            setCanceledOnTouchOutside(true)
            setCancelable(true)
            setUpRecyclerView(binding)
        }

        private fun setUpRecyclerView(binding: DialogListBinding){
            binding.tvTitle.text = title
            if(list.size > 0){
                binding.rvList.layoutManager = LinearLayoutManager(context)
                binding.rvList.setHasFixedSize(true)
                val memberListItemAdapter  = MemberListItemAdapter(MemberListItemAdapter.MembersClickListener{ member->
                    onItemSelected(member)
                    dismiss()
                })
                binding.rvList.adapter = memberListItemAdapter
                memberListItemAdapter.submitList(list)
            }
        }

        protected abstract fun onItemSelected( user : User)
    }