package com.bluetech.vidown.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bluetech.vidown.R

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val button = view.findViewById<Button>(R.id.btn_test)

        button.setOnClickListener {
            val text = view.findViewById<TextView>(R.id.text)

            text.text = "555555"
        }

        return view
    }

    companion object {
    }
}