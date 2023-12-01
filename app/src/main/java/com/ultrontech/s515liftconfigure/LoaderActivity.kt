package com.ultrontech.s515liftconfigure

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class LoaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.horizontal_loader)
        val loaderImageView = findViewById<ImageView>(R.id.loaderImageView)
        (loaderImageView.drawable as AnimationDrawable).start()

    }
}