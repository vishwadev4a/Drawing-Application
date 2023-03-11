package com.thevishuapps.drawingapp

import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import androidx.core.os.postDelayed
import kotlinx.coroutines.NonCancellable.start


class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash )

        val text=findViewById<TextView>(R.id.textView)
        text.setTextColor(Color.WHITE)
        val insta: ImageButton =findViewById(R.id.instagram)
        insta.setOnClickListener{
            val uri: Uri =
                Uri.parse("https://www.instagram.com/vishwadevsharma_/?igshid=YmMyMTA2M2Y%3D")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        val github:ImageButton=findViewById(R.id.github)
        github.setOnClickListener{
            val uri: Uri =
                Uri.parse("https://github.com/vishwadev4a")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        val linkedin:ImageButton=findViewById(R.id.linkedin)
        linkedin.setOnClickListener{
            val uri: Uri =
                Uri.parse("https://www.linkedin.com/in/vishwadev-sharma-aa091923b")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        val start:ImageButton=findViewById(R.id.start_app)
        start.setOnClickListener{
        val i:Intent=Intent(this,MainActivity::class.java)
            startActivity(i)
        }
    }
}