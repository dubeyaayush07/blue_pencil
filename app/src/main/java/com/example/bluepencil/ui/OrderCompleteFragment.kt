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
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
import java.io.IOException


class OrderCompleteFragment : Fragment() {

    private lateinit var binding: FragmentOrderCompleteBinding
    private lateinit var order: Order

    private val IMAGE_UPLOAD_REQUEST_CODE: Int = 2003

    private var url1: String? = null
    private var url2: String? = null
    private var url3: String? = null
    private var count: Int = 1
    private var isComplete: Boolean = false

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        count = order.photoUrls?.size ?: 1

        binding.orderStatus.text = "0 out of $count uploaded"

        binding.submitBtn.setOnClickListener {
            if (isComplete) {
                if (isOnline()) {
                    var user = FirebaseAuth.getInstance().currentUser
                    if (user != null) saveOrder()
                } else {
                    Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                        .show()
                }
            } else {
                Snackbar.make(binding.root, "You need to upload $count images", Snackbar.LENGTH_LONG)
                    .show()
            }

        }




        binding.addBtn.setOnClickListener {
            if (isOnline()) {
                launchUpload()
            } else {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                    .show()

            }
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
            Snackbar.make(binding.root, "Failed to load image", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun uploadPhoto(uri: Uri, user: FirebaseUser) {
        Snackbar.make(binding.root, "Uploading...", Snackbar.LENGTH_LONG)
            .show()
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
                handleUpload(url, uri)
            }

        }.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failed", Snackbar.LENGTH_LONG)
                .show()
            Log.e(OrderFragment.TAG, "Upload Failure")
        }
    }

    private fun handleUpload(url: String, uri: Uri) {
        when {
            url1 == null -> {
                url1 = url
                binding.orderStatus.text = "1 out of $count uploaded"
                binding.imgView1.visibility = View.VISIBLE
                if (count == 1) {
                    binding.addBtn.visibility = View.GONE
                    isComplete = true
                }
                displayImage(binding.imgView1, uri)
            }
            url2 == null -> {
                url2 = url
                binding.orderStatus.text = "2 out of $count uploaded"
                binding.imgView2.visibility = View.VISIBLE
                if (count == 2) {
                    binding.addBtn.visibility = View.GONE
                    isComplete = true
                }
                displayImage(binding.imgView2, uri)
            }
            else -> {
                url3 = url
                binding.orderStatus.text = "3 out of $count uploaded"
                binding.imgView3.visibility = View.VISIBLE
                binding.addBtn.visibility = View.GONE
                isComplete = true
                displayImage(binding.imgView3, uri)

            }
        }

    }

    private fun displayImage(imgView: ImageView, image: Uri) {
        Glide.with(imgView.context)
            .load(image)
            .into(imgView)
    }



    private fun saveOrder() {
        val collection = Firebase.firestore.collection("orders")
        val task = collection.document(order.id.toString())
            .update("jobUrls", getUrlList(), "complete", true)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Order completed", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderCompleteFragmentDirections.actionOrderCompleteFragmentToJobFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Problem occurred please try again", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun getUrlList(): List<String?>? {
       when {
           count == 1 -> return listOf(url1)
           count == 2 -> return listOf(url1, url2)
           count == 3 -> return listOf(url1, url2, url3)
           else -> return null
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