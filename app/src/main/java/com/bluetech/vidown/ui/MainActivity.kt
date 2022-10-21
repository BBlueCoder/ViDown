package com.bluetech.vidown.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bluetech.vidown.R
import com.bluetech.vidown.ui.fragments.DownloadFragment
import com.bluetech.vidown.ui.fragments.MainFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainFragment = MainFragment()
    private val downloadFragment = DownloadFragment()
    private var currentFragment : Fragment = mainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpNavigationBottom()

    }

    private fun setUpNavigationBottom(){

        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host,mainFragment,"Main")
            add(R.id.nav_host,downloadFragment,"Downloads").hide(downloadFragment)
        }.commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mainFragment -> {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment).show(mainFragment).commit()
                    currentFragment = mainFragment
                    true
                }
                R.id.downloadFragment -> {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment).show(downloadFragment).commit()
                    currentFragment = downloadFragment
                    true
                }
                else -> false
            }
        }

    }
}