package xyz.bluepencil.bluepencil.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import xyz.bluepencil.bluepencil.model.Placard
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PlacardAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                   requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.show()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        adapter = PlacardAdapter(PlacardAdapter.OnClickListener { placard, isProfile ->
            if (!isProfile) {
                if (placard.userId == user?.uid) {
                    Snackbar.make(
                        binding.root,
                        "Self Order not Allowed",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToGraphicOrderFragment(placard))
                }
            } else {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment(placard))
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
                binding.loadingBar.visibility = View.GONE
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

            R.id.info -> {
               findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInfoFragment())
            }

            R.id.contact -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToContactFragment())
            }

            R.id.privacy -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bluepencil.xyz/#privacy")).apply {
                    startActivity(this)
                }
            }

            R.id.terms -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bluepencil.xyz/#terms")).apply {
                    startActivity(this)
                }
            }

            R.id.refund -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bluepencil.xyz/#refund")).apply {
                    startActivity(this)
                }
            }

            R.id.pricing -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bluepencil.xyz/#pricing")).apply {
                    startActivity(this)
                }
            }


        }

        return super.onOptionsItemSelected(item)
    }


}