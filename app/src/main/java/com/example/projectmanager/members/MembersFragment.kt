package com.example.projectmanager.members

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentMembersBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User

/**
 * Members fragment shows the members of the board
 */

class MembersFragment : Fragment(), MenuProvider, SearchView.OnQueryTextListener   {

    private lateinit var binding : FragmentMembersBinding
    private lateinit var mBoardDetails : Board
    private lateinit var mProgressDialog : Dialog
    private lateinit var mAssignedMembersList : ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_members,container,false)

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Members"

        val args = MembersFragmentArgs.fromBundle(requireArguments())
        mBoardDetails = args.boardDetails

        showProgressBar()
        FireStore().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)

        return binding.root
    }

    // function called from fire store after fetching the list of the users assigned to board
    fun setUpMembersList(list: ArrayList<User>){

        mAssignedMembersList = list

        binding.recyclerViewMembers.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewMembers.setHasFixedSize(true)

        val memberListItemAdapter  = MemberListItemAdapter(MemberListItemAdapter.MembersClickListener{ member->
            Toast.makeText(context,member.name,Toast.LENGTH_SHORT).show()
        })

        binding.recyclerViewMembers.adapter = memberListItemAdapter
        memberListItemAdapter.submitList(list)

        binding.recyclerViewMembers.visibility = View.VISIBLE
        hideProgressBar()
    }

    // function to show dialog to the user to add any member by putting his/her email id
    private fun dialogSearchMember(){
        val dialog = context?.let { Dialog(it) }!!
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener{
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStore().getMemberDetails(this,email)
            }
            else{
                Toast.makeText(context,"Please enter the email",Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    // function called from fire store class after getting the user of given email
    fun memberDetails(user: User){
        if(user in mAssignedMembersList){
            Toast.makeText(context,"Already a member of Board",Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
        else{
            // add this user to the assigned to list of the board
            mBoardDetails.assignedTo.add(user.id)
            // now we will update this list in the fire store database by input of board details and user
            FireStore().assignMemberToBoard(this,mBoardDetails,user)
        }
    }

    // function called from the fire store class after updating the assigned to list of the board
    fun membersAssignedSuccess(user : User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        setUpMembersList(mAssignedMembersList)
    }

    // setting up the menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.member_fragment_menu,menu)

        val menuSearch = menu.findItem(R.id.searchMembersMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.addMember->{
                dialogSearchMember()
                true
            }
            else -> false
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            binding.recyclerViewMembers.visibility = View.GONE
            FireStore().getAssignedMemberListSearch(this,mBoardDetails.assignedTo,newText)
        }
        return true
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
        binding.membersProgressBar.visibility = View.VISIBLE
    }

    // this will stop showing progress bar when long running task is completed
    fun hideProgressBar(){
        binding.membersProgressBar.visibility = View.GONE
    }
}