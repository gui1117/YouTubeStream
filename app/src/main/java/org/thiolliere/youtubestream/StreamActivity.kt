package org.thiolliere.youtubestream

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLDecoder
import javax.net.ssl.HttpsURLConnection

class StreamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        val link = when {
            intent.action == Intent.ACTION_VIEW -> intent.dataString
            intent.action == Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> {
                Log.e("Error", "unsupported action intent" + intent.action)
                finish()
                return
            }
        }

        Log.i("link", link as String)

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)

        var match = """^.*(?:(?:youtu\.be/|v/|vi/|u/w/|embed/)|(?:(?:watch)?\?v(?:i)?=|&v(?:i)?=))([^#&?]*).*""".toRegex()
                .find(link)

        if (match == null) {
            Toast.makeText(applicationContext, "Invalid Link:\n$link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val videoCode = match.destructured.component1()

        val url = URL("https://youtube.com/get_video_info?video_id=$videoCode&format=json")

        val response = StringBuffer()

        with(url.openConnection() as HttpsURLConnection) {
            val charArray = CharArray(180)
            BufferedReader(InputStreamReader(inputStream)).use {
                var len = it.read(charArray)
                while (len != -1) {
                    response.append(charArray, 0, len)
                    len = it.read(charArray)
                }
            }
        }

        match = """url_encoded_fmt_stream_map=([^&]*)""".toRegex().find(response.toString())

        if (match == null) {
            Toast.makeText(applicationContext, getString(R.string.fail_to_get_stream), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val streamsEncoded = match.destructured.component1()

        if (streamsEncoded == "") {
            Toast.makeText(applicationContext, getString(R.string.fail_to_get_stream), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        var mainURL: Pair<String, String>? = null
        for (stream in URLDecoder.decode(streamsEncoded, "UTF-8").split(",")) {
            val streamFormat = """type=([^#&?]*)""".toRegex().find(stream)!!.destructured.component1()
            //val quality = """quality=([^#&?]*)""".toRegex().find(stream)!!.destructured.component1()
            val streamURL = """url=([^#&?]*)""".toRegex().find(stream)!!.destructured.component1()

            if (mainURL == null) {
                mainURL = Pair(URLDecoder.decode(streamURL, "UTF-8"), URLDecoder.decode(streamFormat, "UTF-8"))
            }
        }

        Log.i("stream", mainURL.toString())

        val (uri, format) = mainURL!!

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setDataAndType(Uri.parse(uri), format)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_activity_not_found).format(format), Toast.LENGTH_LONG).show()
        }

        finish()
    }
}
