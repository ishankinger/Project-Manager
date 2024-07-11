package com.example.projectmanager.boards

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentBoardsBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Boards fragment is home page of this app which contains all boards assigned to the user
 * Here we can navigate to particular boards and can also navigate to create board fragment
 */

class BoardsFragment : Fragment(), MenuProvider, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentBoardsBinding
    private lateinit var boardsListItemAdapter: BoardsListItemAdapter
    private var bottomNavigationView: BottomNavigationView? = null

    private lateinit var mUserName : String
    private lateinit var mSharePreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_boards,container,false)

        bottomNavigationView = activity?.findViewById(R.id.bottomNavigationView)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Boards"

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        mSharePreferences = context?.getSharedPreferences(
            Constants.PROJECT_MANAGER_PREFERENCE,
            Context.MODE_PRIVATE)!!

        val tokenUpdated = mSharePreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated){
            showProgressDialog()
            FireStore().signInRegisteredUser(this)
        }
        else{
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                updateFCMToken(token)
            }
        }

        binding.buttonBoards.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_boardsFragment2_to_boardsCreateFragment)
        }

        return binding.root
    }

    // function called from fire store class after fetching the user details from database
    fun onUpdateBoardsFragment(user : User){
//        bottomNavigationView?.menu?.findItem(R.id.profileFragment2)?.setIcon(R.drawable.color_gradient)
        FireStore().getBoardsList(this)
    }

    // function called from fire store class after fetching the boards list from database
    // here we will connect list with recycler view and adapter
    fun populateBoardsListToUI(boardsList : ArrayList<Board>) {

        hideProgressDialog()

        if(boardsList.size > 0){

            binding.recyclerViewBoards.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewBoards.setHasFixedSize(true)

            boardsListItemAdapter = BoardsListItemAdapter(BoardsListItemAdapter.BoardsClickListener { board->
                findNavController().navigate(BoardsFragmentDirections.actionBoardsFragment2ToTasksFragment(board.documentId))
            })

            binding.recyclerViewBoards.adapter = boardsListItemAdapter
            boardsListItemAdapter.submitList(boardsList)

            binding.recyclerViewBoards.visibility = View.VISIBLE
            binding.noBoards.visibility = View.GONE

        }
        else{
            binding.recyclerViewBoards.visibility = View.GONE
            binding.noBoards.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess(){
        val editor : SharedPreferences.Editor = mSharePreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        FireStore().signInRegisteredUser(this)
    }

    private fun updateFCMToken(token : String){
        val userHashMap = HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog()
        FireStore().updateUserProfileData(this,userHashMap)
    }

    // function providing menu to the fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.boards_fragment_menu,menu)

        val menuSearch = menu.findItem(R.id.searchBoardsMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.addBoardMenu ->{
                Navigation.findNavController(binding.root).navigate(R.id.action_boardsFragment2_to_boardsCreateFragment)
                true
            }
            else-> false
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            binding.recyclerViewBoards.visibility = View.GONE
            FireStore().getBoardListSearch(this,newText)
        }
        return true
    }

    // function to show progress bar when some task is going on
    private fun showProgressDialog(){
        binding.boardsProgressBar.visibility = View.VISIBLE
    }

    // this will stop showing progress bar when long running task is completed
    private fun hideProgressDialog(){
        binding.boardsProgressBar.visibility = View.GONE
    }
}