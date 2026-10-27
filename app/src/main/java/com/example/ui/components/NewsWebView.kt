package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NewsWebView(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onError: (hasError: Boolean, errorMsg: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Hardware layer acceleration for ultra-fast rendering & 60fps scrolling
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            // Enable third-party cookies for modern news portals, CDNs, and media
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // Maximum speed & caching optimizations
                cacheMode = WebSettings.LOAD_DEFAULT
                loadsImagesAutomatically = true
                blockNetworkImage = false

                // Pre-rasterize offscreen content to avoid white flashes during fast scrolling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    offscreenPreRaster = true
                }

                allowFileAccess = false
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                mediaPlaybackRequiresUserGesture = false

                // Optimized Mobile Chrome User-Agent
                val defaultUa = userAgentString
                userAgentString = defaultUa.replace("; wv", "").replace("Version/4.0 ", "")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = false
                }

                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val targetUri = request?.url ?: return false
                    val scheme = targetUri.scheme ?: ""

                    if (scheme == "http" || scheme == "https") {
                        val host = targetUri.host ?: ""
                        if (host.contains("banglatoday24.xyz") || host.contains("banglatoday.xyz")) {
                            return false // Fast in-app loading
                        } else {
                            // External links (social, ads, youtube) open in system browser
                            return try {
                                val intent = Intent(Intent.ACTION_VIEW, targetUri)
                                context.startActivity(intent)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        }
                    } else if (scheme in listOf("tel", "mailto", "sms", "whatsapp")) {
                        return try {
                            val intent = Intent(Intent.ACTION_VIEW, targetUri)
                            context.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            true
                        }
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { onUrlChanged(it) }
                    onError(false, "")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    url?.let { onUrlChanged(it) }
                    view?.title?.let { onTitleChanged(it) }
                    // Inject smooth scrolling
                    view?.evaluateJavascript(
                        "(function(){ document.documentElement.style.scrollBehavior = 'smooth'; })();",
                        null
                    )
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        val desc = error?.description?.toString() ?: "ইন্টারনেট সংযোগ বিচ্ছিন্ন"
                        onError(true, desc)
                    }
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.proceed()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    title?.let { onTitleChanged(it) }
                }
            }

            loadUrl(url)
        }
    }

    DisposableEffect(webView) {
        onWebViewCreated(webView)
        onDispose {
            webView.stopLoading()
        }
    }

    AndroidView(
        factory = { webView },
        update = { wv ->
            val currentWebUrl = wv.url ?: ""
            if (url.isNotBlank() && currentWebUrl != url && !urlsEquivalent(currentWebUrl, url)) {
                wv.loadUrl(url)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun urlsEquivalent(u1: String, u2: String): Boolean {
    val clean1 = u1.trimEnd('/')
    val clean2 = u2.trimEnd('/')
    return clean1.equals(clean2, ignoreCase = true)
}
