package com.example.bluepencil

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.bluepencil.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PlacardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PlacardAdapter(PlacardAdapter.OnClickListener {
            if (it != null) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOrderFragment(it))
            }
        })
        binding.placardList.adapter = adapter
        fetchPlacards()
    }

    private fun fetchPlacards() {
        val db = Firebase.firestore
        db.collection("placards")
            .get()
            .addOnSuccessListener { result ->
                adapter.data = result.toObjects(Placard::class.java)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMainFragment())
            }
        }

        return super.onOptionsItemSelected(item)
    }


}