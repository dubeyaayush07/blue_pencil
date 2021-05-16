package xyz.bluepencil.bluepencil


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import xyz.bluepencil.bluepencil.databinding.FragmentGraphicOrderCompleteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import xyz.bluepencil.bluepencil.model.Order
import java.io.IOException


class GraphicOrderComplete : Fragment() {

    private lateinit var binding: FragmentGraphicOrderCompleteBinding
    private lateinit var order: Order

    private val IMAGE_UPLOAD_REQUEST_CODE: Int = 2003

    companion object {
        const val TAG = "GraphicOrderCompleteFragment"
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
        binding.uploadTxt.text = "Order Count ${order.count}"

        binding.submitBtn.setOnClickListener {
            if (binding.jobUrlTxt.text.isNullOrBlank() ||
                !URLUtil.isValidUrl(binding.jobUrlTxt.text.toString())) {
                Snackbar.make(binding.root, "Valid Job Url Required", Snackbar.LENGTH_LONG)
                    .show()
            } else if (!isOnline()) {
                Snackbar.make(binding.root, "Internet Connection Required", Snackbar.LENGTH_LONG)
                    .show()
            } else saveOrder(binding.jobUrlTxt.text.toString())

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