package com.example.projectmanager.firebase

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.projectmanager.auth.SignInFragment
import com.example.projectmanager.auth.SignUpFragment
import com.example.projectmanager.boards.BoardsCreateFragment
import com.example.projectmanager.boards.BoardsFragment
import com.example.projectmanager.cards.CardsDetailsFragment
import com.example.projectmanager.cards.CardsFragment
import com.example.projectmanager.members.MembersFragment
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.profile.ProfileFragment
import com.example.projectmanager.tasks.TasksFragment
import com.example.projectmanager.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStore {

    private val mFireStore = FirebaseFirestore.getInstance()

    // function to return the current user id ( uid of the firebase )
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    // This will store the users details in the cloud fireStore
    fun registerUser(fragment : SignUpFragment, userInfo : User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                fragment.userRegisteredSuccess()
            }
    }

    // This function will help in getting user's information so is very useful
    fun signInRegisteredUser(fragment : Fragment){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {
                val loggedInUser = it.toObject(User::class.java)!!
                when(fragment){
                    is SignInFragment ->{
                        fragment.signInSuccess(loggedInUser)
                    }
                    is ProfileFragment ->{
                        fragment.updateProfileUsersDetails(loggedInUser)
                    }
                    is BoardsFragment ->{
                        fragment.onUpdateBoardsFragment(loggedInUser)
                    }
                    is BoardsCreateFragment ->{
                        fragment.createBoard(loggedInUser)
                    }
                }
            }
    }

    // function to update the user's data in the fire store cloud
    fun updateUserProfileData(fragment: Fragment, userHashMap : HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener{
                when(fragment){
                    is ProfileFragment->{
                        fragment.profileUpdateSuccess()
                    }
                }
            }
    }

    // function called when create button is clicked and all the information about the board is extracted
    fun createBoard(fragment : BoardsCreateFragment,board: Board){
        mFireStore.collection(Constants.BOARD)
            .document()
            .set(board,SetOptions.merge())
            .addOnSuccessListener {
                fragment.boardCreatedSuccessfully()
            }
    }

    // function to get the board list for a particular user so that we can display boards on main screen
    // now we will take all boards whose 'assigned to' is equal to uid of the user
    // also store the uid of the board in the document id
    fun getBoardsList(fragment: Fragment){
        mFireStore.collection(Constants.BOARD)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                val boardList : ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                when(fragment){
                    is BoardsFragment->{
                        fragment.populateBoardsListToUI(boardList)
                    }
                    is CardsFragment->{
                        fragment.getBoardsList(boardList)
                    }
                }
            }
    }

    // function to get the board details from a particular board using it's board document id
    fun getBoardDetails(fragment : TasksFragment, boardDocumentId : String){
        mFireStore.collection(Constants.BOARD)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener{
                document->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                fragment.boardDetails(board)
            }
            .addOnFailureListener{
                fragment.hideProgressDialog()
                Toast.makeText(fragment.context,"Error updating board list", Toast.LENGTH_SHORT).show()
            }
    }

    // function to delete the board
    fun deleteBoard(fragment: TasksFragment, boardDocumentId: String){
        mFireStore.collection(Constants.BOARD)
            .document(boardDocumentId)
            .delete()
            .addOnSuccessListener {
                fragment.onDeleteBoard()
            }
    }

    // function to update the task list in the fire store database
    fun addUpdateTaskList(fragment: Fragment, board: Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener{
                when(fragment){
                    is TasksFragment ->{
                        fragment.addUpdateTaskListSuccess()
                    }
                    is CardsDetailsFragment ->{
                        fragment.addUpdateTaskListSuccess()
                    }
                }
            }
            .addOnFailureListener {
                when(fragment){
                    is TasksFragment ->{
                        fragment.hideProgressDialog()
                        Toast.makeText(fragment.context,"Error updating task list", Toast.LENGTH_SHORT).show()
                    }
                    is CardsDetailsFragment ->{
                        fragment.hideProgressDialog()
                        Toast.makeText(fragment.context,"Error updating card", Toast.LENGTH_SHORT).show()
                    }
                }

            }
    }

    // function to get the list of the users from assigned list of the board which contains the user ids
    // of all the persons who are assigned to a particular project
    fun getAssignedMembersListDetails(fragment : Fragment, assignedTo : ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener{
                    document->
                val userList : ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                when(fragment){
                    is MembersFragment->{
                        fragment.setUpMembersList(userList)
                    }
                    is CardsDetailsFragment->{
                        fragment.getAssignedMembersDetailList(userList)
                    }
                    is TasksFragment->{
                        fragment.getAssignedMembersDetailList(userList)
                    }
                }
            }
            .addOnFailureListener {
                when(fragment){
                    is MembersFragment->{
                        fragment.hideProgressBar()
                        Toast.makeText(fragment.context,"Error updating member list", Toast.LENGTH_SHORT).show()
                    }
                    is CardsDetailsFragment->{
                        fragment.hideProgressBar()
                        Toast.makeText(fragment.context,"Error updating member list", Toast.LENGTH_SHORT).show()
                    }
                    is TasksFragment->{
                        fragment.hideProgressBar()
                        Toast.makeText(fragment.context,"Error updating member list", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // function to get the details of the member having the email id input to this function
    fun getMemberDetails(fragment : MembersFragment, email : String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                    document->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    fragment.memberDetails(user)
                }
                else{
                    fragment.hideProgressDialog()
                    Toast.makeText(fragment.activity,"No such member found",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(fragment.activity,"Error while getting user details",Toast.LENGTH_SHORT).show()
            }
    }

    // function to update the 'assigned to' list of the board
    fun assignMemberToBoard(fragment : MembersFragment, board: Board, user : User){
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener{
                fragment.membersAssignedSuccess(user)
            }
            .addOnFailureListener {
                fragment.hideProgressDialog()
                Toast.makeText(fragment.activity,"Error updating member list",Toast.LENGTH_SHORT).show()
            }
    }


}