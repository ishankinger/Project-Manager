package com.example.projectmanager.tasks

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentTasksBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.Task
import com.example.projectmanager.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Task fragment contains different list in horizontal recycler view and each list itself contains
 * some cards inside it which contains different tasks to be performed by the members of that board
 */

class TasksFragment : Fragment(), MenuProvider {

    private lateinit var binding : FragmentTasksBinding
    private lateinit var boardDocumentId : String
    private lateinit var mProgressDialog : Dialog
    lateinit var mBoardDetails : Board
    private lateinit var snapHelper: PagerSnapHelper
    private var bottomNavigationView: BottomNavigationView? = null
    lateinit var mMembersDetailList : ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tasks,container,false)

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Initialize the BottomNavigationView instance and hide the BottomNavigationView
        bottomNavigationView = activity?.findViewById(R.id.bottomNavigationView)
        bottomNavigationView?.visibility = View.GONE

        val args = TasksFragmentArgs.fromBundle(requireArguments())
        boardDocumentId = args.boardId

        snapHelper = PagerSnapHelper()

        showProgressBar()
        FireStore().getBoardDetails(this,boardDocumentId)

        binding.scrollFab.setOnClickListener {
            snapHelper.attachToRecyclerView(null)
            binding.scrollFab.visibility = View.GONE
            binding.restrictScrollFab.visibility = View.VISIBLE
        }

        binding.restrictScrollFab.setOnClickListener {
            snapHelper.attachToRecyclerView(binding.recyclerViewTasks)
            binding.scrollFab.visibility = View.VISIBLE
            binding.restrictScrollFab.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        // Show the BottomNavigationView when the fragment is paused
        bottomNavigationView?.visibility = View.VISIBLE
        // Show the Action Bar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Boards"
    }

    // this function is called from the fire store class (get board details function)
    fun boardDetails(board: Board) {

        // the board details coming from fire store class is stored in this local variable
        mBoardDetails = board

        (activity as? AppCompatActivity)?.supportActionBar?.title = mBoardDetails.name

        FireStore().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)

    }

    // function called from Fire store class after fetching the assigned member details from board id
    fun getAssignedMembersDetailList(userList : ArrayList<User>){
        mMembersDetailList = userList

        // adding the first task as the add list button which will work to add another list
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewTasks.setHasFixedSize(true)

        val taskListItemAdapter = TaskListItemAdapter(mBoardDetails.taskList.size,this,TaskListItemAdapter.TasksClickListener{ task->
            Toast.makeText(context,"List : ${task.title}",Toast.LENGTH_SHORT).show()
        })

        binding.recyclerViewTasks.adapter = taskListItemAdapter
        taskListItemAdapter.submitList(mBoardDetails.taskList)

        hideProgressBar()
        binding.recyclerViewTasks.visibility = View.VISIBLE
    }

    // function called finally from fire store class after updating the task list
    fun addUpdateTaskListSuccess() {

        // again show progress dialog as we will now show correct task list by calling board details function of fire store class
        FireStore().getBoardDetails(this, mBoardDetails.documentId)
    }

    // function to add list in the tasks array of a particular board
    fun createTaskList(taskListName: String) {

        val task = Task(taskListName, FireStore().getCurrentUserID())

        // adding the task list to the first of the array
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressBar()
        binding.recyclerViewTasks.visibility = View.INVISIBLE

        FireStore().addUpdateTaskList(this, mBoardDetails)
    }

    // function called to update the tasks of a particular board
    fun updateTaskList(position: Int, listName: String, model: Task) {

        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressBar()
        binding.recyclerViewTasks.visibility = View.INVISIBLE

        FireStore().addUpdateTaskList(this, mBoardDetails)
    }

    // function called to delete the tasks of a particular board
    private fun deleteTaskList(position: Int) {

        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressBar()
        binding.recyclerViewTasks.visibility = View.INVISIBLE

        FireStore().addUpdateTaskList(this, mBoardDetails)
    }

    // function to show alert dialog when delete list is clicked
    fun alertDialogForDeleteList(position: Int, title: String) {

        val builder = context?.let { AlertDialog.Builder(it) }!!
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

           deleteTaskList(position)
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()
    }

    // function to add card to the particular task
    fun addCardToTaskList(position : Int, cardName : String){

        // remove add list
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUserList : ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FireStore().getCurrentUserID())

        val card = Card(cardName,FireStore().getCurrentUserID(),cardAssignedUserList)
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        // updated task
        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        // updating old task with the new one
        mBoardDetails.taskList[position] = task

        showProgressBar()
        binding.recyclerViewTasks.visibility = View.INVISIBLE
        FireStore().addUpdateTaskList(this, mBoardDetails)

    }

    // function to update the cards position after drag and drop of the cards
    fun updateCardsInTaskList(taskListPosition : Int, cards : ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressBar()
        binding.recyclerViewTasks.visibility = View.INVISIBLE
        FireStore().addUpdateTaskList(this,mBoardDetails)
    }

    // function called from firestore after deleting the board
    fun onDeleteBoard(){
        hideProgressDialog()
        binding.root.findNavController().popBackStack(R.id.boardsFragment2,false)
    }

    // functions to provide menu to this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.tasks_fragment_menu,menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.memberMenuTasks ->{
                findNavController().navigate(TasksFragmentDirections.actionTasksFragmentToMembersFragment(mBoardDetails))
                true
            }
            R.id.deleteMenuTasks ->{
                val builder = context?.let { AlertDialog.Builder(it) }!!
                builder.setTitle("Alert")
                builder.setMessage("Are you sure you want to delete this board")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("Yes") { dialogInterface, which ->
                    dialogInterface.dismiss()
                    showProgressDialog("")
                    FireStore().deleteBoard(this,boardDocumentId)
                }
                builder.setNegativeButton("No") { dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
                true
            }
            R.id.editMenuTasks ->{
                Navigation.findNavController(binding.root).navigate(R.id.action_tasksFragment_to_updateBoardsFragment)
                true
            }
            else-> false
        }
    }

    // function to show progress dialog box when some task is going on
    private fun showProgressDialog(text : String){
        mProgressDialog = context?.let { Dialog(it) }!!
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.progressBarText).text = text
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
    }

    // this will stop showing dialog box when long running task is completed
     fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    // function to show progress bar when some task is going on
    private fun showProgressBar(){
        binding.tasksProgressBar.visibility = View.VISIBLE
    }

    // this will stop showing progress bar when long running task is completed
     fun hideProgressBar(){
        binding.tasksProgressBar.visibility = View.GONE
    }
}