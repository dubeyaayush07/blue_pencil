package xyz.bluepencil.bluepencil.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.databinding.FragmentGraphicOrderBinding
import xyz.bluepencil.bluepencil.model.Order
import xyz.bluepencil.bluepencil.model.Placard
import xyz.bluepencil.bluepencil.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.IOException

class GraphicOrderFragment : Fragment() {

    private lateinit var binding: FragmentGraphicOrderBinding
    private lateinit var placard: Placard
    private var user: User? = null


    companion object {
        const val TAG = "GraphicOrderFragment"
    }

    private val UPI_PAYMENT: Int = 12345


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_graphic_order, container, false)
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        val collection = Firebase.firestore.collection("users")
        val fUser = FirebaseAuth.getInstance().currentUser
        collection.whereEqualTo("uid", "${fUser?.uid}").get()
            .addOnSuccessListener { document ->
                user = if(document.isEmpty) {
                    null
                } else {
                    document.toObjects(User::class.java)[0]
                }
            }
        placard = GraphicOrderFragmentArgs.fromBundle(requireArguments()).selectedPlacard
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
    }

    private fun payUsingUpi() {
        if (checkOrderCount()) return
        else if (binding.specificationTxt.text.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                "Order Specification required",
                Snackbar.LENGTH_LONG
            ).show()
            return
        } else if (user == null){
            Snackbar.make(
                binding.root,
                "Unable to fetch user detail",
                Snackbar.LENGTH_LONG
            ).show()
            return
        } else if (user?.freeCount!! > 0 && placard.free == true){
            user!!.freeCount = user!!.freeCount?.minus(1)
            val collection = Firebase.firestore.collection("users")
            collection.document(user!!.id.toString()).update("freeCount", user!!.freeCount)
                .addOnSuccessListener {  saveOrder(user!!.uid.toString()) }
                .addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "Unable to fetch user detail",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            return
        }


        val amount = placard.cost

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

    private fun checkOrderCount(): Boolean {
        if (binding.orderCount.text.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                "Order Count required",
                Snackbar.LENGTH_LONG
            ).show()
            return true
        } else if (binding.orderCount.text.toString().toInt() <= 0
            || binding.orderCount.text.toString().toInt() > 100) {
            Snackbar.make(
                binding.root,
                "Order Count should be between 1 and 100",
                Snackbar.LENGTH_LONG
            ).show()
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UPI_PAYMENT) {
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
                saveOrder(user!!.uid.toString())
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

    private fun saveOrder(userId: String) {
        val collection = Firebase.firestore.collection("orders")

        val order = Order(
            userId = userId,
            editorId = placard.userId,
            count = binding.orderCount.text.toString().toInt(),
            remark = binding.specificationTxt.text.toString(),
            link = binding.linkTxt.text.toString(),
            type = placard.type,
            editorName = placard.userName
        )
        val task = collection.add(order)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Order submitted successfully", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigate(GraphicOrderFragmentDirections.actionGraphicOrderFragmentToInboxFragment())
        }

        task.addOnFailureListener {
            Snackbar.make(binding.root, "Failed to take your order please contact support", Snackbar.LENGTH_LONG)
                .show()
        }
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