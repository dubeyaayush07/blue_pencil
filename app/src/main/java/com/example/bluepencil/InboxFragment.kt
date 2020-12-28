package com.example.bluepencil

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.bluepencil.databinding.FragmentInboxBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class InboxFragment : Fragment() {

    companion object {
        const val TAG = "InboxFragment"
    }

    private lateinit var binding: FragmentInboxBinding
    private lateinit var adapter: OrderInboxAdapter
    private lateinit var userId: String


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
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                adapter.data = result.toObjects(Order::class.java)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }


}