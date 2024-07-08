package com.example.projectmanager.cards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentCardsBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.Card

class CardsFragment : Fragment() {

    private lateinit var binding : FragmentCardsBinding
    private lateinit var mBoardList : ArrayList<Board>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_cards,container,false)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Cards"

        showProgressBar()
        FireStore().getBoardsList(this)


        return binding.root
    }

    fun getBoardsList(boardList : ArrayList<Board>){
        mBoardList = boardList
        val cardListUser : ArrayList<Card> = ArrayList()
        for(board in boardList){
            for(task in board.taskList){
                for(card in task.cards){
                    if(card.assignedTo.contains(FireStore().getCurrentUserID())){
                        cardListUser.add(card)
                    }
                }
            }
        }
        binding.recyclerViewCards.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCards.setHasFixedSize(true)
        val userCardListItemAdapter = UserCardListItemAdapter(UserCardListItemAdapter.UserCardClickListener {
            Toast.makeText(context,"Card clicked",Toast.LENGTH_SHORT).show()
        })
        binding.recyclerViewCards.adapter = userCardListItemAdapter
        userCardListItemAdapter.submitList(cardListUser)
        binding.recyclerViewCards.visibility = View.VISIBLE
        hideProgressBar()
    }


    // function to show progress bar when some task is going on
    private fun showProgressBar(){
        binding.cardsProgressBar.visibility = View.VISIBLE
    }

    // this will stop showing progress bar when long running task is completed
    fun hideProgressBar(){
        binding.cardsProgressBar.visibility = View.GONE
    }

}