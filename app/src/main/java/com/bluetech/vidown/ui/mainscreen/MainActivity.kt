package com.bluetech.vidown.ui.mainscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bluetech.vidown.R
import com.bluetech.vidown.ui.downloadscreen.DownloadFragment
import com.bluetech.vidown.ui.downloadscreen.DownloadViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainFragment = MainFragment()
    private val downloadFragment = DownloadFragment()
    private var currentFragment : Fragment = mainFragment

    private lateinit var downloadViewModel : DownloadViewModel
    private lateinit var mainViewModel : MainViewModel

    private lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadViewModel = ViewModelProvider(this)[DownloadViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setUpNavigationBottom()

        startDownloadWorkerIfDownloadsUncompleted()
    }

    private fun startDownloadWorkerIfDownloadsUncompleted(){
        lifecycleScope.launch(Dispatchers.IO) {
            if(mainViewModel.isThereAnyUncompletedDownloads())
                mainViewModel.startDownloadWorker()
        }
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

        bottomNav = findViewById(R.id.bottom_nav)

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