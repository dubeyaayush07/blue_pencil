package xyz.bluepencil.bluepencil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import xyz.bluepencil.bluepencil.databinding.ActivityPaymentBinding
import xyz.bluepencil.bluepencil.model.Order
import java.io.IOException


class PaymentActivity : AppCompatActivity(), PaymentResultListener {
    private val TAG = "PaymentActivity"
    private val UPI_PAYMENT: Int = 12345

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var order: Order


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment)
        order = PaymentActivityArgs.fromBundle(intent.extras!!).selectedOrder
        val amount = PaymentActivityArgs.fromBundle(intent.extras!!).cost

        binding.specification.text = order.remark
        binding.cost.text = getCurrencyString(amount)
        binding.name.text = order.editorName

        Checkout.preload(applicationContext)
        binding.pay.setOnClickListener {
           if (isOnline()) {
               hidePayButtons()
               if (amount == 0) saveOrder()
               else payUsingUpi(amount)
           } else {
               Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                   .show()
           }
        }

        binding.payOther.setOnClickListener {
            if (isOnline()) {
                hidePayButtons()
                if (amount == 0) saveOrder()
                else getOrderId(amount)
            } else {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    }



    private fun payUsingUpi(amount: Int) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", "9131455619@okbizaxis")
            .appendQueryParameter("pn", "Blue Pencil")
            .appendQueryParameter("tn", "Order Payment")
            .appendQueryParameter("am", "$amount")
            .appendQueryParameter("cu", "INR")
            .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        startActivityForResult(chooser, UPI_PAYMENT)
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
            showPayButtons()
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
                saveOrder()
                Log.e("UPI", "payment successfull: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
                Snackbar.make(binding.root, "Payment cancelled by user.", Snackbar.LENGTH_SHORT)
                    .show()
                showPayButtons()
                Log.e("UPI", "Cancelled by user: $approvalRefNo")
            } else {
                Snackbar.make(
                    binding.root,
                    "Transaction failed. Please try again",
                    Snackbar.LENGTH_SHORT
                ).show()
                showPayButtons()
                Log.e("UPI", "failed payment: $approvalRefNo")
            }
        } else {
            Log.e("UPI", "Internet issue: ")
            showPayButtons()
            Snackbar.make(
                binding.root,
                "Internet connection is not available. Please check and try again",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    private fun startPayment(orderId: String, amount: Int) {
        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val activity:Activity = this
        val co = Checkout()
        co.setKeyID("rzp_live_NJ3S6ygiPYmpOC")

        try {
            val options = JSONObject()
            options.put("name", "Blue Pencil")
            options.put("order_id", orderId)

            options.put("currency", "INR")
            options.put("amount", "${amount * 100}") //pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            co.open(activity, options)
        } catch (e: Exception) {
            showPayButtons()
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    override fun onPaymentSuccess(razorpayPaymentID: String) {
        try {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT)
                .show()
            saveOrder()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    private fun saveOrder() {
        val collection = Firebase.firestore.collection("orders")
        val task = collection.add(order)

        task.addOnSuccessListener {
            Snackbar.make(binding.root, "Order submitted successfully", Snackbar.LENGTH_LONG)
                .show()
        }

        task.addOnFailureListener {
            showPayButtons()
            Snackbar.make(binding.root, "Failed to take your order please contact support", Snackbar.LENGTH_LONG)
                .show()
        }
    }


    override fun onPaymentError(code: Int, response: String) {
        try {
            showPayButtons()
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentError", e)
        }
    }

    private fun getOrderId(amount: Int) {

        binding.loadingBar.visibility = View.VISIBLE
        OrderApi.retrofitService.getOrderID("$amount").enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                binding.loadingBar.visibility = View.GONE
                showPayButtons()
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                binding.loadingBar.visibility = View.GONE
                startPayment(response.body().toString(), amount)
            }
        })
    }

    private fun hidePayButtons() {
        binding.payOther.isEnabled = false
        binding.pay.isEnabled = false
    }

    private fun showPayButtons() {
        binding.payOther.isEnabled = true
        binding.pay.isEnabled = true
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