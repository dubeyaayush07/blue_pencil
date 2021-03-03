package com.example.bluepencil

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
import com.example.bluepencil.databinding.FragmentGraphicOrderCompleteBinding
import com.example.bluepencil.model.Order
import com.example.bluepencil.ui.OrderCompleteFragmentArgs
import com.example.bluepencil.ui.OrderCompleteFragmentDirections
import com.example.bluepencil.ui.OrderFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException


class GraphicOrderComplete : Fragment() {

    private lateinit var binding: FragmentGraphicOrderCompleteBinding
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
            R.layout.fragment_graphic_order_complete, container, false)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        order = GraphicOrderCompleteArgs.fromBundle(requireArguments()).selectedOrder
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isOnline()) {
            launchUpload()
        } else {
            Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(GraphicOrderCompleteDirections.actionGraphicOrderCompleteToJobFragment())

        }
    }

    private fun launchUpload() {
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
                    if (user != null && uri != null) uploadPhoto(uri, user)
                }
            }
        } else {
            Snackbar.make(binding.root, "Operation Failed", Snackbar.LENGTH_LONG)
                .show()

            findNavController().navigate(GraphicOrderCompleteDirections.actionGraphicOrderCompleteToJobFragment())
        }
    }

    private fun uploadPhoto(uri: Uri, user: FirebaseUser) {
        val storageReference = FirebaseStorage.getInstance().reference

        val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)
        binding.progressBar.visibility = View.VISIBLE

        uploadTask.addOnProgressListener{
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            binding.progressBar.progress = progress.toInt()

        }.addOnSuccessListener {
            binding.progressBar.visibility = View.GONE
            val downloadUrl = imageRef.downloadUrl
            downloadUrl.addOnSuccessListener {
                val url = it.toString()
                saveOrder(url)
            }

        }.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failed", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(GraphicOrderCompleteDirections.actionGraphicOrderCompleteToJobFragment())
        }
    }

    private fun saveOrder(url: String) {
        val collection = Firebase.firestore.collection("orders")
        val task = collection.document(order.id.toString())
            .update("jobUrls", listOf(url), "complete", true)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Order completed", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(GraphicOrderCompleteDirections.actionGraphicOrderCompleteToJobFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Problem occurred please try again", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(GraphicOrderCompleteDirections.actionGraphicOrderCompleteToJobFragment())
        }
    }


    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }



}