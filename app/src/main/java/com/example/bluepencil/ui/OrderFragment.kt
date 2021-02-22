package com.example.bluepencil.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bluepencil.R
import com.example.bluepencil.databinding.FragmentOrderBinding
import com.example.bluepencil.model.Order
import com.example.bluepencil.model.Placard
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException


class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var placard: Placard

    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001

    companion object {
        const val TAG = "OrderFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order, container, false)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        placard = OrderFragmentArgs.fromBundle(requireArguments()).selectedPlacard
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uploadAndPayBtn.setOnClickListener {
            if (isOnline()) {
                launchUpload()
            } else {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                    .show()

            }

        }
    }


    fun launchUpload() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data != null) {
                    val uri = data.data
                    var user = FirebaseAuth.getInstance().currentUser
                    if (user != null) uploadPhoto(uri.toString(), user)

                }
            }
        } else {
            findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToHomeFragment())
        }
    }


    private fun uploadPhoto(path: String, user: FirebaseUser) {
        Snackbar.make(binding.root, "Uploading...", Snackbar.LENGTH_LONG)
            .show()

        val storageReference = FirebaseStorage.getInstance().getReference()
        val uri = Uri.parse(path)

        val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)
        binding.progressBar.visibility = View.VISIBLE


        uploadTask.addOnProgressListener{
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            binding.progressBar.progress = progress.toInt()

        }.addOnSuccessListener {
            val downloadUrl = imageRef.downloadUrl
            downloadUrl.addOnSuccessListener {
                val url = it.toString()
                saveOrder(url, user.uid)
            }

        }.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failure", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToHomeFragment())
            Log.e(TAG, "Upload Failure")
        }
    }

    private fun saveOrder(photoUrl: String, userId: String) {
        val collection = Firebase.firestore.collection("orders")
        var remark: String = "Beautify"
        if (!binding.remarkTxt.text.isNullOrBlank()) {
            remark = binding.remarkTxt.text.toString()
        }
        val order = Order(
            userId = userId,
            editorId = placard.userId,
            photoUrl = photoUrl,
            remark = remark
        )
        val task = collection.add(order)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Photo Uploaded", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToHomeFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Photo Upload Failure", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToHomeFragment())

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