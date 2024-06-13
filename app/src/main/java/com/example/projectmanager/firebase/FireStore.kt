package com.example.projectmanager.firebase

import androidx.fragment.app.Fragment
import com.example.projectmanager.auth.SignInFragment
import com.example.projectmanager.auth.SignUpFragment
import com.example.projectmanager.boards.BoardsCreateFragment
import com.example.projectmanager.boards.BoardsFragment
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.profile.ProfileFragment
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
    // now we will take all boards whose assigned to is equal to uid of the user
    // also store the uid of the board in the document id
    fun getBoardsList(fragment: BoardsFragment){
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
                fragment.populateBoardsListToUI(boardList)
            }
    }

}