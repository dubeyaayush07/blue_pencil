package xyz.bluepencil.bluepencil.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
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
import kotlinx.android.synthetic.main.fragment_graphic_order.*
import java.io.IOException

class GraphicOrderFragment : Fragment() {

    private lateinit var binding: FragmentGraphicOrderBinding
    private lateinit var placard: Placard
    private var user: User? = null


    companion object {
        const val TAG = "GraphicOrderFragment"
    }

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
                checkOrderDetails()
            } else {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                    .show()

            }
        }
    }

    private fun checkOrderDetails() {
        if (checkOrder()) return
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
                .addOnSuccessListener {
                    val amount = placard.cost * (binding.orderCount.text.toString().toInt() - 1)
                    sendOrder(amount)
                }
                .addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "Unable to fetch user detail",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            return
        }

        val amount = placard.cost * binding.orderCount.text.toString().toInt()
        sendOrder(amount)
    }

    private fun sendOrder(amount: Int) {
        if (binding.promoCode.text.isNullOrBlank()) {
            val order = getOrder()
            findNavController().navigate(GraphicOrderFragmentDirections.actionGraphicOrderFragmentToPaymentActivity(order, amount))
        } else if (binding.promoCode.text.toString().equals("GET15BULK")) {
            val count = binding.orderCount.text.toString().toInt()
            if (count < 10) {
                Snackbar.make(
                    binding.root,
                    "You need to order at least 10 graphics",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                val newAmount = amount - ((amount * 15) / 100)
                val order = getOrder()
                findNavController().navigate(GraphicOrderFragmentDirections.actionGraphicOrderFragmentToPaymentActivity(order, newAmount))
            }
        } else {
            Snackbar.make(
                binding.root,
                "Invalid Promo Code",
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    private fun getOrder(): Order {
        return Order(
            userId = user!!.uid.toString(),
            editorId = placard.userId,
            count = binding.orderCount.text.toString().toInt(),
            remark = binding.specificationTxt.text.toString(),
            link = binding.linkTxt.text.toString(),
            type = placard.type,
            editorName = placard.userName
        )
    }

    private fun checkOrder(): Boolean {
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
        } else if (!binding.linkTxt.text.isNullOrBlank() &&
                !URLUtil.isValidUrl(binding.linkTxt.text.toString())) {
            Snackbar.make(
                binding.root,
                "Invalid Resource Link",
                Snackbar.LENGTH_LONG
            ).show()
            return true
        }
        return false
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