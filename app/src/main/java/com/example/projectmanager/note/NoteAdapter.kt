package com.example.projectmanager.note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.databinding.ListNoteLayoutBinding
import com.example.projectmanager.models.Note

class NoteAdapter(private val clickListener: NoteClickListener) : ListAdapter<Note,
        NoteAdapter.ViewHolder>(NoteDiffCallBack()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListNoteLayoutBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Note, clickListener: NoteClickListener) {
            binding.noteDesc.text = item.noteDesc
            binding.noteTitle.text = item.noteTitle
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListNoteLayoutBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class NoteDiffCallBack : DiffUtil.ItemCallback<Note>(){
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return  oldItem.id == newItem.id &&
                    oldItem.noteDesc == newItem.noteDesc &&
                    oldItem.noteTitle == newItem.noteTitle
        }
    }

    class NoteClickListener(val clickListener: (note: Note) -> Unit) {
        fun onClick(note: Note) = clickListener(note)
    }

}
