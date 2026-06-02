package com.clinic.radar

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.JsPromptResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var localServer: LocalWebServer? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val LOCAL_PORT = 8787
        private const val FILE_CHOOSER_REQUEST = 1001
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

            override fun onShowFileChooser(
                view: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = callback
                val intent = params.createIntent()
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                } catch (e: Exception) {
                    fileUploadCallback = null
                    return false
                }
                return true
            }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST) {
            val results = if (resultCode == Activity.RESULT_OK) {
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            } else {
                null
            }
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
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
