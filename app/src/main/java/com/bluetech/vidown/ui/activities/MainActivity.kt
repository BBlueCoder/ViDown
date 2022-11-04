package com.bluetech.vidown.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluetech.vidown.R
import com.bluetech.vidown.ui.fragments.DownloadFragment
import com.bluetech.vidown.ui.fragments.MainFragment
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.utils.Constants.DOWNLOAD_SERVICE_ACTION
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainFragment = MainFragment()
    private val downloadFragment = DownloadFragment()
    private var currentFragment : Fragment = mainFragment

    private lateinit var downloadViewModel : DownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadViewModel = ViewModelProvider(this)[DownloadViewModel::class.java]

        setUpNavigationBottom()

    }

    private fun setUpNavigationBottom(){

        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host,downloadFragment,"Downloads").hide(downloadFragment)
            add(R.id.nav_host,mainFragment,"Main")
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

    override fun onStart() {
        super.onStart()
        //LocalBroadcastManager.getInstance(this).registerReceiver(DownloadReceiver(), IntentFilter(DOWNLOAD_SERVICE_ACTION))
    }

    inner class DownloadReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val result = intent?.getStringExtra("result")
            println("received broadcast action")
            if(result == "success"){

                //downloadViewModel.refreshDownload()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(DownloadReceiver())
    }

}