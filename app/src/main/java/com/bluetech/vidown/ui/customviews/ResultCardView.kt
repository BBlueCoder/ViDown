package com.bluetech.vidown.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bluetech.vidown.R
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class ResultCardView(context : Context,attrs : AttributeSet) : MaterialCardView(context,attrs) {
    init {
        inflate(context, R.layout.result_card_view,this)
    }

    fun setThumbnail(url : String){
        val cardThumbnail = findViewById<ImageView>(R.id.card_thumbnail)

        Glide
            .with(context)
            .load(url)
            .into(cardThumbnail)
    }

    fun setTitle(title : String){
        val cardTitle = findViewById<TextView>(R.id.card_title)

        cardTitle.text = title
    }
}