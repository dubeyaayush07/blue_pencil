package xyz.bluepencil.bluepencil.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.databinding.FragmentProfileBinding
import xyz.bluepencil.bluepencil.model.Placard


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var placard: Placard

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.bottomNavView)
        bottomNavigationView.visibility = View.GONE
        placard = OrderFragmentArgs.fromBundle(requireArguments()).selectedPlacard
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.name.text = placard.userName
        binding.description.text = placard.description
        binding.tag.text = if (placard.type == "photo") "Photo Editor" else "Graphic Designer"
        val adapter = ProfileAdapter()
        binding.photoList.adapter = adapter
        adapter.data = placard.photoList as List<String>
        setupTags()
    }

    private fun setupTags() {
        binding.tag1.text = placard.tags!![0]
        binding.tag2.text = placard.tags!![1]
        binding.tag3.text = placard.tags!![2]
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }


}