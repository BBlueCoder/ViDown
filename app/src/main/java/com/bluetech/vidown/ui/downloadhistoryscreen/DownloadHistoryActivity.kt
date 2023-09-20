package com.bluetech.vidown.ui.downloadhistoryscreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: DownloadHistoryViewModel

    private lateinit var adapter: DownloadHistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_history)

        viewModel = ViewModelProvider(this)[DownloadHistoryViewModel::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.history_recycler_view)
        adapter = DownloadHistoryAdapter(this,
            {
            viewModel.removeDownloadHistoryItem(it)
         },
        {
            viewModel.cancelDownload(it)
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = null
        recyclerView.adapter = adapter

        val backIcon = findViewById<ImageView>(R.id.history_back_arrow)
        backIcon.setOnClickListener {
            finish()
        }

        observeDownloadHistory()
    }

    private fun observeDownloadHistory(){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.getDownloadHistory.collect {
                    adapter.submitList(it)
                }
            }
        }
    }
}