package com.epiahackers.pelisfavorites.component

import android.content.Context
import android.view.View
import com.epiahackers.pelisfavorites.R
import com.google.android.material.textview.MaterialTextView

class ScoreTextView (context: Context, attrs:android.util.AttributeSet? = null): MaterialTextView(context, attrs) {

    override fun setText(text: CharSequence?, type: BufferType?) {

        val score: Int? = text.toString().toIntOrNull()

        if(score == null){

            visibility = View.GONE

            return super.setText(text, type)

        }else visibility = View.VISIBLE

        setBackgroundColor(context.getColor(when(score){
            in 0..4 -> R.color.lowScore
            5,6 -> R.color.middleScore
            7,8 -> R.color.goodScore
            9,10 -> R.color.highScore
            else -> R.color.white
        }))

        super.setText(text.toString() + context.getString(R.string.score_value), type)
    }

}