package com.bluetech.vidown.ui.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.ui.recyclerviews.ResultsAdapter
import com.bluetech.vidown.ui.customviews.ResultCardView
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.services.DownloadFileService
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var circularProgress : CircularProgressIndicator
    private lateinit var lookUpBtn : Button
    private lateinit var showAvailableFormatsBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        circularProgress = view.findViewById(R.id.main_progress)

        observeSearchResults(view)

        lookUpBtn = view.findViewById(R.id.btn_look_up)

        showAvailableFormatsBtn = view.findViewById(R.id.card_result_available_formats)
        showAvailableFormatsBtn.paintFlags = showAvailableFormatsBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        //link.setText("https://www.youtube.com/watch?v=Q3IkQxzpFfw")

        lookUpBtn.setOnClickListener {
            val link = view.findViewById<TextInputEditText>(R.id.link_text)
            link.clearFocus()
            val url = link.text.toString()

            //check if input is a valid url
            if (!url.matches(Regex("(https)?(:\\/\\/)?(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.([a-zA-Z0-9()]{1,6})(?=)\\/.+"))) {
                view.snackBar(resources.getString(R.string.input_is_not_an_url))
                return@setOnClickListener
            }
            circularProgress.visibility = View.VISIBLE
            lookUpBtn.visibility = View.GONE

            lifecycleScope.launch {
                viewModel.searchForResult(url)
            }
        }

        showAvailableFormatsBtn.setOnClickListener {
            println("available formats click!!!")
            Navigation.findNavController(requireActivity() ,R.id.nav_host).navigate(R.id.show_available_formats)
        }

        return view
    }

    private fun observeSearchResults(view: View) {
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lookUpResults.collect { result ->
                    circularProgress.visibility= View.GONE
                    lookUpBtn.visibility = View.VISIBLE
                    result.onFailure { exp ->
                        view.snackBar(
                            exp.message!!
                        )
                    }
                    result.onSuccess {
                        if(it.isEmpty())
                            return@onSuccess
                        val itemInfo = it.filterIsInstance<ResultItem.ItemInfo>().first()
                        showItemInfoCard(view,itemInfo)
                        showAvailableFormats(view)
                    }
                }
            }
        }

    }

    private fun showItemInfoCard(view: View, itemInfo: ResultItem.ItemInfo) {
        val resultCard = view.findViewById<ResultCardView>(R.id.card_result)
        if (resultCard.visibility == View.VISIBLE)
            return

        resultCard.setThumbnail(itemInfo.thumbnail)
        resultCard.setTitle(itemInfo.title)

        resultCard.visibility = View.VISIBLE

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
        resultCard.startAnimation(anim)

    }

    private fun showAvailableFormats(view: View){
        if(!showAvailableFormatsBtn.isVisible){

            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
            showAvailableFormatsBtn.visibility = View.VISIBLE
            showAvailableFormatsBtn.startAnimation(anim)
            return
        }


    }

}