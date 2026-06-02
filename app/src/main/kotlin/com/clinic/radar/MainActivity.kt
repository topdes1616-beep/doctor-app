package com.clinic.radar

import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var localServer: LocalWebServer? = null

    companion object {
        private const val LOCAL_PORT = 8787
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        localServer = LocalWebServer(this, LOCAL_PORT)
        localServer?.start()

        webView = findViewById(R.id.webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("موافق") { _, _ -> result.confirm() }
                    .setOnCancelListener { result.cancel() }
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("موافق") { _, _ -> result.confirm() }
                    .setNegativeButton("إلغاء") { _, _ -> result.cancel() }
                    .setOnCancelListener { result.cancel() }
                    .show()
                return true
            }

            override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String?, result: JsPromptResult): Boolean {
                val input = EditText(this@MainActivity)
                input.setText(defaultValue ?: "")
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("موافق") { _, _ -> result.confirm(input.text.toString()) }
                    .setNegativeButton("إلغاء") { _, _ -> result.cancel() }
                    .setOnCancelListener { result.cancel() }
                    .show()
                return true
            }
        }

        webView.loadUrl("http://localhost:$LOCAL_PORT/")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localServer?.stop()
    }
}
