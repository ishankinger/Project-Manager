package com.example.projectmanager.boards

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentBoardsBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User

class BoardsFragment : Fragment() {

    private lateinit var binding : FragmentBoardsBinding
    private lateinit var mProgressDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_boards,container,false)

        showProgressDialog(" ")
        FireStore().signInRegisteredUser(this)

        binding.boardsFragmentImage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_boardsFragment2_to_profileFragment2)
        }

        binding.buttonBoards.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_boardsFragment2_to_boardsCreateFragment)
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.boards_fragment_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    fun onUpdateBoardsFragment(user : User){

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.boardsFragmentImage)
        Toast.makeText(context,"HELLO1",Toast.LENGTH_LONG).show()
        FireStore().getBoardsList(this)
    }

    fun populateBoardsListToUI(boardsList : ArrayList<Board>) {
        Toast.makeText(context,"HELLO2",Toast.LENGTH_LONG).show()
        hideProgressDialog()

        if(boardsList.size > 0){

            // show the boards recycler view and remove no_boards text from the screen
            binding.recyclerViewBoards.visibility = View.VISIBLE
            binding.noBoards.visibility = View.GONE

            // making linear layout of recycler views
            binding.recyclerViewBoards.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewBoards.setHasFixedSize(true)

            // then connecting the recycler view's adapter to adapter that we have made
            val adapter = context?.let { BoardsListItemAdapter(it, boardsList) }
            binding.recyclerViewBoards.adapter = adapter

            // adding the onclick listener to the boards
            adapter?.setOnClickListener(object : BoardsListItemAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    Toast.makeText(context,"Board is clicked",Toast.LENGTH_SHORT).show()
                }
            })
        }
        else{
            binding.recyclerViewBoards.visibility = View.GONE
            binding.noBoards.visibility = View.VISIBLE
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
    private fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

}