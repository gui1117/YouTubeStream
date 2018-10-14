package org.thiolliere.youtubestream

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getFromClipBoard(view: View) {
        var intent = Intent(this, StreamActivity::class.java).apply {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val link = clipboard.primaryClip.getItemAt(0).text
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(intent)
    }
}
