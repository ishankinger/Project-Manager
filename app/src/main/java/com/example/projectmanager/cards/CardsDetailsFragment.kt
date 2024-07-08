package com.example.projectmanager.cards

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentCardsDetailsBinding
import com.example.projectmanager.dialog.LabelColorListDialog
import com.example.projectmanager.dialog.MembersSelectListDialog
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.SelectedMembers
import com.example.projectmanager.models.Task
import com.example.projectmanager.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Cards fragment shows the information about the card and also can update it
 */

class CardsDetailsFragment : Fragment(), MenuProvider {

    private lateinit var binding : FragmentCardsDetailsBinding

    private lateinit var mProgressDialog : Dialog

    private var mTaskListPosition : Int = -1
    private var mCardPosition : Int = -1

    private lateinit var mBoardDetails : Board

    private var mSelectedColor = ""
    private var mSelectedDueDateMilliSeconds : Long = 0
    private lateinit var mMembersDetailList : ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_cards_details,container,false)

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        val args = CardsDetailsFragmentArgs.fromBundle(requireArguments())
        mTaskListPosition = args.taskListPosition
        mBoardDetails = args.boardDetails

        val card : Card = args.card
        for(ind in mBoardDetails.taskList[mTaskListPosition].cards.indices){
            if(mBoardDetails.taskList[mTaskListPosition].cards[ind] == card){
                mCardPosition = ind
                break
            }
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title = card.name

        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if(mSelectedDueDateMilliSeconds > 0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding.tvSelectDueDate.text = selectedDate
        }

        showProgressBar()
        FireStore().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)

        binding.btnUpdateCardDetails.setOnClickListener {
            if(binding.etNameCardDetails.text.toString().isNotEmpty()){
                updateCardDetails()
            }
            else{
                Toast.makeText(context,"Please enter a card name",Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }

        binding.tvSelectMembers.setOnClickListener{
            membersListDialog()
        }

        return binding.root
    }

    // function called after fetching the details of the user assigned to the cards
    fun getAssignedMembersDetailList(userList : ArrayList<User>){

        mMembersDetailList = userList

        setupSelectedMembersList()

        binding.cardDetailsView.visibility = View.VISIBLE
        hideProgressBar()
    }

    // function called after updating the card list
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        findNavController().navigate(CardsDetailsFragmentDirections.actionCardsDetailsFragmentToTasksFragment(mBoardDetails.documentId))
    }

    // function to update the card details called by clicking the update button
    private fun updateCardDetails(){

        // here first we have to remove the last task list that is 'add list' as when we will again go back to
        // the taskList Activity we will load the details by calling getBoardDetails function which will again add
        // the task list activity
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val card = Card(
            binding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStore().addUpdateTaskList(this,mBoardDetails)

    }

    // color list for the label color to be shown
    private fun colorsList() : ArrayList<String>{
        val colorsList : ArrayList<String> = ArrayList()
        colorsList.add("#E6E6FA")
        colorsList.add("#CBC3E3")
        colorsList.add("#CF9FFF")
        colorsList.add("#AA98A9")
        colorsList.add("#E0B0FF")
        colorsList.add("#915F6D")
        colorsList.add("#770737")
        colorsList.add("#800080")
        colorsList.add("#7F00FF")

        return colorsList
    }

    // function to set up the color
    private fun setColor(){
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    // function to show the dialog of different colors
    private fun labelColorListDialog(){
        // getting the list of color from the color list function
        val colorList : ArrayList<String> = colorsList()

        // making a listDialog ( from label color list item adapter )
        val listDialog = object : LabelColorListDialog(
            context,
            colorList,
            "Select Label Color",
            mSelectedColor){

            // change mSelected Color to the color selected and then setColor function to change the color
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }

        // showing the list dialog
        listDialog.show()
    }

    // function to show the calendar to pick the date
    private fun showDataPicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = context?.let {
            DatePickerDialog(
                it,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                    val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                    binding.tvSelectDueDate.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val theDate = sdf.parse(selectedDate)
                    mSelectedDueDateMilliSeconds = theDate!!.time
                },
                year,
                month,
                day
            )
        }
        dpd?.show() // It is used to show the datePicker Dialog.
    }

    // function to show the member list dialog
    private fun membersListDialog(){

        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size > 0){
            // all the users who are assigned to cards have made selected = true
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }
        else{
            for(i in mMembersDetailList.indices){
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersSelectListDialog(
            context,
            mMembersDetailList,
            "Select Members"
        ){
            override fun onItemSelected(user: User) {
                if(!user.selected){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }
                else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }

        listDialog.show()

    }

    // function to set up the member list in that card in the select members space
    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

        // extracting the selected members from the assigned member list
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

        // submitting this selected member list to the cardMemberListItem adapter
        if(selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(context,6)

            val cardMemberListItemAdapter = CardMembersListItemAdapter(selectedMembersList.size,true,
                CardMembersListItemAdapter.CardMembersClickListener{
                membersListDialog()
            })

            binding.rvSelectedMembersList.adapter = cardMemberListItemAdapter
            cardMemberListItemAdapter.submitList(selectedMembersList)
        }
        else{
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    // function to delete the card
    private fun deleteCard(){
        val cardList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardPosition)

        // to get rid of add card element
        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStore().addUpdateTaskList(this,mBoardDetails)
    }

    // called by clicking delete icon on menu
    private fun alertDialogForDeleteList(cardName : String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete the card : ${cardName}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // function for creating menu items on fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.cards_details_fragment_menu,menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.deleteCardMenu->{
                alertDialogForDeleteList(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                true
            }
            else -> false
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
        binding.cardsDetailsProgressBar.visibility = View.VISIBLE
    }

    // this will stop showing progress bar when long running task is completed
    fun hideProgressBar(){
        binding.cardsDetailsProgressBar.visibility = View.GONE
    }

}