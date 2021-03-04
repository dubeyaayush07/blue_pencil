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
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
import kotlin.random.Random


class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var placard: Placard

    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val UPI_PAYMENT: Int = 12345

    private var url1: String? = null
    private var url2: String? = null
    private var url3: String? = null
    private var count: Int = 0

    companion object {
        const val TAG = "OrderFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order, container, false)
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        placard = OrderFragmentArgs.fromBundle(requireArguments()).selectedPlacard
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.orderBtn.setOnClickListener {
            if (isOnline()) {
                payUsingUpi()
            } else {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
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

    private fun payUsingUpi() {
        if (count == 0) {
            Snackbar.make(
                binding.root,
                "You need to select at least one image",
                Snackbar.LENGTH_LONG
            ).show()
            return
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) saveOrder(user.uid)
            return
        }

        val amount = placard.cost * count

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", "9131455619@okbizaxis")
            .appendQueryParameter("pn", placard.userName)
            .appendQueryParameter("tn", "Order Payment")
            .appendQueryParameter("am", "$amount")
            .appendQueryParameter("cu", "INR")
            .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        startActivityForResult(chooser, UPI_PAYMENT)

    }


    private fun launchUpload() {
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
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null && uri != null) uploadPhoto(uri, user)
                }
            } else if (requestCode == UPI_PAYMENT) {
                if (data != null) {
                    val trxt = data.getStringExtra("response")
                    Log.e("UPI", "onActivityResult: $trxt")
                    val dataList: ArrayList<String?> = ArrayList()
                    dataList.add(trxt)
                    upiPaymentDataOperation(dataList)
                } else {
                    Log.e("UPI", "onActivityResult: " + "Return data is null")
                    val dataList: ArrayList<String?> = ArrayList()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList)
                }
            }
        } else {
            Snackbar.make(binding.root, "Operation Failed", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String?>) {
        if (true) {
            var str = data[0]
            Log.e("UPIPAY", "upiPaymentDataOperation: $str")
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&").toTypedArray()
            for (i in response.indices) {
                val equalStr = response[i].split("=").toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0].toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            if (status == "success") {
                //Code to handle successful transaction here.
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) saveOrder(user.uid)
                Log.e("UPI", "payment successfull: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
                Snackbar.make(binding.root, "Payment cancelled by user.", Snackbar.LENGTH_SHORT)
                    .show()
                Log.e("UPI", "Cancelled by user: $approvalRefNo")
            } else {
                Snackbar.make(
                    binding.root,
                    "Transaction failed. Please try again",
                    Snackbar.LENGTH_SHORT
                ).show()
                Log.e("UPI", "failed payment: $approvalRefNo")
            }
        } else {
            Log.e("UPI", "Internet issue: ")
            Snackbar.make(
                binding.root,
                "Internet connection is not available. Please check and try again",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadPhoto(uri: Uri, user: FirebaseUser) {
        Snackbar.make(binding.root, "Uploading...", Snackbar.LENGTH_LONG)
            .show()

        val storageReference = FirebaseStorage.getInstance().reference

        val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)
        binding.progressBar.visibility = View.VISIBLE

        uploadTask.addOnProgressListener {
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
            Snackbar.make(binding.root, "Photo Upload Failure", Snackbar.LENGTH_LONG)
                .show()
            Log.e(TAG, "Upload Failed")
        }
    }

    private fun handleUpload(url: String, uri: Uri) {
        when {
            url1 == null -> {
                url1 = url
                count = 1
                binding.imgView1.visibility = View.VISIBLE
                displayImage(binding.imgView1, uri)
            }
            url2 == null -> {
                url2 = url
                count = 2
                binding.imgView2.visibility = View.VISIBLE
                displayImage(binding.imgView2, uri)
            }
            else -> {
                url3 = url
                count = 3
                binding.imgView3.visibility = View.VISIBLE
                binding.addBtn.visibility = View.GONE
                displayImage(binding.imgView3, uri)

            }
        }
    }

    private fun displayImage(imgView: ImageView, image: Uri) {
        Glide.with(imgView.context)
            .load(image)
            .into(imgView)
    }


    private fun saveOrder(userId: String) {
        val collection = Firebase.firestore.collection("orders")
        var remark: String = "Beautify"
        if (!binding.remarkTxt.text.isNullOrBlank()) {
            remark = binding.remarkTxt.text.toString()
        }
        val order = Order(
            userId = userId,
            editorId = placard.userId,
            photoUrls = getUrlList(),
            remark = remark,
            type = placard.type,
            editorName = placard.userName
        )
        val task = collection.add(order)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Order submitted successfully", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToInboxFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Failed to take your order please contact support", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun getUrlList(): List<String?> {
        if (count == 1) return listOf(url1)
        else if (count == 2) return listOf(url1, url2)
        else return listOf(url1, url2, url3)
    }

    private fun isOnline(): Boolean {
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