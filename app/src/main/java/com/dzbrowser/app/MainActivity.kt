package com.dzbrowser.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnReload: ImageButton
    private lateinit var btnHome: ImageButton

    private val homeUrl = "https://www.google.com"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnReload = findViewById(R.id.btnReload)
        btnHome = findViewById(R.id.btnHome)

        setupWebView()
        setupToolbar()

        webView.loadUrl(homeUrl)
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.setSupportZoom(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = android.view.View.VISIBLE
                updateNavButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = android.view.View.GONE
                if (!addressBar.isFocused) {
                    addressBar.setText(url ?: "")
                }
                updateNavButtons()
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                val safeDescription = description ?: "Unknown error"
                val html = """
                    <html><body style="font-family:sans-serif;text-align:center;margin-top:20vh;color:#333;">
                    <h2>This page could not be loaded</h2>
                    <p>$safeDescription</p>
                    </body></html>
                """.trimIndent()
                view?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        }

        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }

    private fun setupToolbar() {
        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnReload.setOnClickListener { webView.reload() }
        btnHome.setOnClickListener { webView.loadUrl(homeUrl) }

        addressBar.setOnEditorActionListener { _, actionId, event ->
            val pressedEnter = event != null &&
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            if (actionId == EditorInfo.IME_ACTION_GO || pressedEnter) {
                val input = addressBar.text.toString()
                webView.loadUrl(resolveInput(input))
                addressBar.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun updateNavButtons() {
        btnBack.isEnabled = webView.canGoBack()
        btnForward.isEnabled = webView.canGoForward()
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.35f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.35f
    }

    private fun resolveInput(rawInput: String): String {
        val input = rawInput.trim()
        if (input.isEmpty()) return homeUrl

        val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(input)
        if (hasScheme) return input

        val looksLikeDomain = Regex("^[^\\s]+\\.[a-zA-Z]{2,}(/.*)?$").matches(input)
        if (looksLikeDomain) return "https://$input"

        val query = java.net.URLEncoder.encode(input, "UTF-8")
        return "https://www.google.com/search?q=$query"
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
