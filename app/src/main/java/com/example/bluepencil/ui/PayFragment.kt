package com.example.bluepencil.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bluepencil.R


class PayFragment : Fragment() {

    private val UPI_PAYMENT: Int = 12345


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val send: Button = view.findViewById(R.id.send)
        val amount: EditText = view.findViewById(R.id.amount_et)
        val note: EditText = view.findViewById(R.id.note)
        val name: EditText = view.findViewById(R.id.name)
        val upiVirtualId: EditText = view.findViewById(R.id.upi_id)

        send.setOnClickListener { //Getting the values from the EditTexts
            if (TextUtils.isEmpty(name.text.toString().trim { it <= ' ' })) {
                Toast.makeText(context, " Name is invalid", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(upiVirtualId.getText().toString().trim())) {
                Toast.makeText(context, " UPI ID is invalid", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(note.text.toString().trim { it <= ' ' })) {
                Toast.makeText(context, " Note is invalid", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(amount.text.toString().trim { it <= ' ' })) {
                Toast.makeText(context, " Amount is invalid", Toast.LENGTH_SHORT).show()
            } else {
                payUsingUpi(
                    name.text.toString(), upiVirtualId.getText().toString(),
                    note.text.toString(), amount.text.toString()
                )
            }
        }


    }

    fun payUsingUpi(name: String, upiId: String, note: String, amount: String) {
        Log.e("main ", "name $name--up--$upiId--$note--$amount")
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name) //.appendQueryParameter("mc", "")
            //.appendQueryParameter("tid", "02125412")
            //.appendQueryParameter("tr", "25584584")
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR") //.appendQueryParameter("refUrl", "blueapp")
            .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri
        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        // check if intent resolves

        startActivityForResult(chooser, UPI_PAYMENT)

    }



     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("main ", "response $resultCode")
        when (requestCode) {
            UPI_PAYMENT -> if (RESULT_OK === resultCode || resultCode == 11) {
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
            } else {
                //when user simply back without payment
                Log.e("UPI", "onActivityResult: " + "Return data is null")
                val dataList: ArrayList<String?> = ArrayList()
                dataList.add("nothing")
                upiPaymentDataOperation(dataList)
            }
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
                Toast.makeText(context, "Transaction successful.", Toast.LENGTH_SHORT)
                    .show()
                Log.e("UPI", "payment successfull: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
                Toast.makeText(context, "Payment cancelled by user.", Toast.LENGTH_SHORT)
                    .show()
                Log.e("UPI", "Cancelled by user: $approvalRefNo")
            } else {
                Toast.makeText(
                    context,
                    "Transaction failed.Please try again",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("UPI", "failed payment: $approvalRefNo")
            }
        } else {
            Log.e("UPI", "Internet issue: ")
            Toast.makeText(
                context,
                "Internet connection is not available. Please check and try again",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



}