package com.example.bluepencil

import android.app.Activity.RESULT_OK
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
import com.example.bluepencil.databinding.FragmentOrderBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask


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
        binding.uploadBtn.setOnClickListener {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
            }
        }

        return binding.root
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
        }
    }


    private fun uploadPhoto(path: String, user: FirebaseUser) {

        val storageReference = FirebaseStorage.getInstance().getReference()
        val uri = Uri.parse(path)

        val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)
        binding.uploadBar.visibility = View.VISIBLE

        uploadTask.addOnProgressListener{
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            binding.uploadBar.progress = progress.toInt()

        }.addOnSuccessListener {
            val downloadUrl = imageRef.downloadUrl
            downloadUrl.addOnSuccessListener {
                val url = it.toString()
                binding.uploadBar.visibility = View.GONE
                saveOrder(url, user.uid)
            }

        }.addOnFailureListener {
            binding.uploadBar.visibility = View.GONE
            Log.e(TAG, "Upload Failure")
        }
    }

    private fun saveOrder(photoUrl: String, userId: String) {
        val collection = Firebase.firestore.collection("orders")
        val order = Order(userId = userId, editorId = placard.userId, photoUrl = photoUrl)
        val task = collection.add(order)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Photo Uploaded", Snackbar.LENGTH_LONG)
                .show()
        }
    }

}