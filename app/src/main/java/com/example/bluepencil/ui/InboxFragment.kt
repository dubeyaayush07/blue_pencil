package com.example.bluepencil.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bluepencil.R
import com.example.bluepencil.databinding.FragmentInboxBinding
import com.example.bluepencil.model.Order
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class InboxFragment : Fragment() {

    companion object {
        const val TAG = "InboxFragment"
    }

    private lateinit var binding: FragmentInboxBinding
    private lateinit var adapter: OrderInboxAdapter
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(InboxFragmentDirections.actionInboxFragmentToHomeFragment())
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_inbox, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: " "
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.VISIBLE
        adapter = OrderInboxAdapter()
        binding.orderList.adapter = adapter
        fetchOrders()
    }

    private fun fetchOrders() {
        val db = Firebase.firestore
        db.collection("orders")
            .orderBy("date", Query.Direction.DESCENDING)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                adapter.data = result.toObjects(Order::class.java)
                if (adapter.itemCount == 0) {
                    binding.emptyTxt.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }


}