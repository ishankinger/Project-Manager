package com.example.projectmanager.tasks

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.cards.CardListItemAdapter
import com.example.projectmanager.databinding.ItemTasksBinding
import com.example.projectmanager.models.Task
import java.util.Collections

/**
 * Adapter of task list items and contains a lot of functionality of views
 */
class TaskListItemAdapter(private val listSize : Int,
                          private val fragment: TasksFragment,
                          private val clickListener: TasksClickListener) : ListAdapter<Task,
        TaskListItemAdapter.ViewHolder>(TasksDiffCallBack()){

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener,position,listSize,fragment,mPositionDraggedTo,mPositionDraggedFrom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemTasksBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(
            model: Task,
            clickListener: TasksClickListener,
            position: Int,
            listSize: Int,
            fragment: TasksFragment,
            mPDT: Int,
            mPDF: Int
        ) {

            /**
             * Different functionalities of tasks list here called
             */
            // showing Add list or not
            if(position == listSize-1){
                binding.tvAddTaskList.visibility = View.VISIBLE
                binding.llTaskItem.visibility = View.GONE
            }
            else{
                binding.tvAddTaskList.visibility = View.GONE
                binding.llTaskItem.visibility = View.VISIBLE
            }

            // name of task list
            binding.tvTaskListTitle.text = model.title

            // Addition of task list or not
            binding.tvAddTaskList.setOnClickListener {
                binding.tvAddTaskList.visibility = View.GONE
                binding.cvAddTaskListName.visibility = View.VISIBLE
                binding.cvAddTaskListName.requestFocus()
                val imm = fragment.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.cvAddTaskListName, InputMethodManager.SHOW_IMPLICIT)
            }
            binding.ibCloseListName.setOnClickListener{
                binding.tvAddTaskList.visibility = View.VISIBLE
                binding.cvAddTaskListName.visibility = View.GONE
            }
            binding.ibDoneListName.setOnClickListener {
                val listName = binding.etTaskListName.text.toString()
                if(listName.isNotEmpty()){
                    fragment.createTaskList(listName)
                }
            }

            // Edit the name of the task list
            binding.ibEditListName.setOnClickListener {
                binding.etEditTaskListName.setText(model.title)
                binding.llTitleView.visibility = View.GONE
                binding.cvEditTaskListName.visibility = View.VISIBLE
                binding.cvEditTaskListName.requestFocus()
                val imm = fragment.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.cvEditTaskListName, InputMethodManager.SHOW_IMPLICIT)
            }
            binding.ibCloseEditableView.setOnClickListener {
                binding.llTitleView.visibility = View.VISIBLE
                binding.cvEditTaskListName.visibility = View.GONE
            }
            binding.ibDoneEditListName.setOnClickListener {
                val listName = binding.etEditTaskListName.text.toString()
                if(listName.isNotEmpty()){
                    fragment.updateTaskList(position,listName,model)
                }
            }

            // deleting the task list
            binding.ibDeleteList.setOnClickListener {
                fragment.alertDialogForDeleteList(position,model.title)
            }
            binding.tvAddCard.setOnClickListener {
                binding.tvAddCard.visibility = View.GONE
                binding.cvAddCard.visibility = View.VISIBLE
            }
            binding.ibCloseCardName.setOnClickListener {
                binding.tvAddCard.visibility = View.VISIBLE
                binding.cvAddCard.visibility = View.GONE
            }

            // adding the card to the list
            binding.ibDoneCardName.setOnClickListener {
                val cardName = binding.etCardName.text.toString()
                if(cardName.isNotEmpty()){
                    fragment.addCardToTaskList(position,cardName)
                }
            }


            /**
             * Card list adapter defined here inside the Task list adapter
             */
            binding.rvCardList.layoutManager = LinearLayoutManager(fragment.context)
            binding.rvCardList.setHasFixedSize(true)

            val cardListItemAdapter =
                fragment.context?.let {
                    CardListItemAdapter(it,fragment.mMembersDetailList,CardListItemAdapter.CardsClickListener{ card->
                        fragment.findNavController().navigate(TasksFragmentDirections
                            .actionTasksFragmentToCardsDetailsFragment(position,fragment.mBoardDetails,card))
                    })
                }

            binding.rvCardList.adapter = cardListItemAdapter
            cardListItemAdapter?.submitList(model.cards)

            binding.task = model
            binding.clickListener = clickListener
            binding.executePendingBindings()


            /**
             * Drag and Drop feature of the cards
             */
            val dividerItemDecoration = DividerItemDecoration(fragment.context,
                DividerItemDecoration.VERTICAL)
            binding.rvCardList.addItemDecoration(dividerItemDecoration)

            var mPositionDraggedFrom = mPDF
            var mPositionDraggedTo = mPDT

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,0
                ){
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val dragPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition

                        if(mPositionDraggedFrom == -1){
                            mPositionDraggedFrom = dragPosition
                        }
                        mPositionDraggedTo = targetPosition

                        Collections.swap(model.cards,dragPosition,targetPosition)

                        cardListItemAdapter?.notifyItemMoved(dragPosition,targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if(mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo){
                            fragment.updateCardsInTaskList(position,model.cards)
                        }
                        mPositionDraggedTo = -1
                        mPositionDraggedFrom = -1
                    }

                }
            )
            helper.attachToRecyclerView(binding.rvCardList)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                // changing the layout params of the recycler list
                val layoutParams = LinearLayout.LayoutParams(
                    (parent.width*0.8).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins( (15.toDp()).toPx() ,0, (40.toDp()).toPx(), 0)
                val binding = ItemTasksBinding.inflate(layoutInflater,parent,false)
                binding.root.layoutParams = layoutParams
                return ViewHolder(binding)
            }
            private fun Int.toDp() :
                    Int = (this / Resources.getSystem().displayMetrics.density).toInt()
            private fun Int.toPx() :
                    Int = (this * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

    class TasksDiffCallBack : DiffUtil.ItemCallback<Task>(){
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return  oldItem.title == newItem.title &&
                    oldItem.createdBy == newItem.createdBy
        }
    }

    class TasksClickListener(val clickListener: (task: Task) -> Unit) {
        fun onClick(task: Task) = clickListener(task)
    }
}
