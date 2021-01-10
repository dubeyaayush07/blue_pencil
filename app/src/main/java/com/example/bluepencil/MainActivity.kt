package com.example.bluepencil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bluepencil.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupViews()
    }

    private fun setupViews() {
        // Finding the Navigation Controller
        var navController = findNavController(R.id.fragNavHost)

        // Setting Navigation Controller with the BottomNavigationView
        binding.bottomNavView.setupWithNavController(navController)

        // Setting Up ActionBar with Navigation Controller
        var appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf (
                R.id.homeFragment,
                R.id.inboxFragment,
                R.id.jobFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        var navController = findNavController(R.id.fragNavHost)
        return navController.navigateUp()
    }





}