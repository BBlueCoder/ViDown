package com.bluetech.vidown.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluetech.vidown.R
import com.bluetech.vidown.core.pojoclasses.DownloadMediaProgress
import com.bluetech.vidown.core.workers.DownloadFileWorker
import com.bluetech.vidown.ui.fragments.DownloadFragment
import com.bluetech.vidown.ui.fragments.MainFragment
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.Constants.DOWNLOAD_FILE_PROGRESS_ACTION
import com.bluetech.vidown.utils.Constants.DOWNLOAD_SERVICE_ACTION
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainFragment = MainFragment()
    private val downloadFragment = DownloadFragment()
    private var currentFragment : Fragment = mainFragment

    private lateinit var downloadViewModel : DownloadViewModel
    private lateinit var mainViewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadViewModel = ViewModelProvider(this)[DownloadViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setUpNavigationBottom()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
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
                    mainViewModel.getLastFavorites()
                    mainViewModel.getLastDownloads()
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

    override fun onResume() {
        super.onResume()
        handleSendIntent(intent)

    }

    private fun handleSendIntent(intent : Intent?){
        if(intent?.action.equals(Intent.ACTION_SEND) && intent?.type.equals("text/plain")){
            val link = intent?.getStringExtra(Intent.EXTRA_TEXT)

            link?.let {
                mainViewModel.updateMediaLink(it)
            }
        }
    }



}