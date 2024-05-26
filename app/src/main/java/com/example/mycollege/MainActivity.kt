package com.example.mycollege

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mycollege.vm.CoursetableViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var coursetableViewModel: CoursetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        coursetableViewModel = ViewModelProvider(this).get(CoursetableViewModel::class.java)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost_main)
            as NavHostFragment

        navController = navHost.navController

        val bottomNavigation: BottomNavigationView = findViewById(R.id.BottomNavMenu)
        bottomNavigation.setupWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}