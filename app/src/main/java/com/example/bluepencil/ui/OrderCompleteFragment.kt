package com.example.bluepencil.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.bluepencil.model.Order
import com.example.bluepencil.R
import com.example.bluepencil.databinding.FragmentOrderCompleteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class OrderCompleteFragment : Fragment() {

    private lateinit var binding: FragmentOrderCompleteBinding
    private lateinit var order: Order

    private val IMAGE_UPLOAD_REQUEST_CODE: Int = 2003

    companion object {
        const val TAG = "OrderCompleteFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_order_complete, container, false)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        order = OrderCompleteFragmentArgs.fromBundle(requireArguments()).selectedOrder
        launchUpload()
        return binding.root
    }

    fun launchUpload() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_UPLOAD_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_UPLOAD_REQUEST_CODE) {
                if (data != null && data.data != null) {
                    val uri = data.data
                    var user = FirebaseAuth.getInstance().currentUser
                    if (user != null) uploadPhoto(uri.toString(), user)

                }
            }
        } else {
            findNavController().navigate(OrderCompleteFragmentDirections.actionOrderCompleteFragmentToJobFragment())
        }
    }

    private fun uploadPhoto(path: String, user: FirebaseUser) {

        val storageReference = FirebaseStorage.getInstance().getReference()
        val uri = Uri.parse(path)

        val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnProgressListener{
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            binding.progressBar.progress = progress.toInt()
            binding.progressTxt.text = "${progress.toInt()} %"

        }.addOnSuccessListener {
            val downloadUrl = imageRef.downloadUrl
            downloadUrl.addOnSuccessListener {
                val url = it.toString()
                saveOrder(url)
            }

        }.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failed", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderCompleteFragmentDirections.actionOrderCompleteFragmentToJobFragment())
            Log.e(OrderFragment.TAG, "Upload Failure")
        }
    }

    private fun saveOrder(photoUrl: String) {
        val collection = Firebase.firestore.collection("orders")
        val task = collection.document(order.id.toString())
            .update("jobUrl", photoUrl, "complete", true)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Photo Uploaded", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderCompleteFragmentDirections.actionOrderCompleteFragmentToJobFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failed", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderCompleteFragmentDirections.actionOrderCompleteFragmentToJobFragment())
        }
    }


}