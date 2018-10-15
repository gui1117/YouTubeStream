package org.thiolliere.youtubestream

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getFromClipBoard(view: View) {
        val intent = Intent(this, StreamActivity::class.java).apply {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (!clipboard.hasPrimaryClip()) {
                Toast.makeText(applicationContext, getString(R.string.no_clip), Toast.LENGTH_SHORT).show()
                return
            }
            val link = clipboard.primaryClip!!.getItemAt(0).text
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(intent)
    }
}
