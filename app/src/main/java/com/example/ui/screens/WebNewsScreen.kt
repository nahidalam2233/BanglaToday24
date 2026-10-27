package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.NewsCategories
import com.example.data.preferences.AppPreferences
import com.example.ui.components.CategoryBar
import com.example.ui.components.NewsTopAppBar
import com.example.ui.components.NewsWebView
import com.example.ui.components.OfflineErrorView
import com.example.util.rememberNetworkStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WebNewsScreen(
    initialUrl: String = NewsCategories.BASE_URL,
    onNavigateToBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val preferences = remember { AppPreferences.getInstance(context) }

    // Real-time network connectivity listener
    val isOnline by rememberNetworkStatus()
    var wasOffline by remember { mutableStateOf(false) }
    var showRestoredBanner by remember { mutableStateOf(false) }

    var currentUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("বাংলা টুডে ২৪") }
    var progress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val bookmarks by preferences.bookmarks.collectAsState()
    val isBookmarked = bookmarks.any { it.url == currentUrl }

    // React to network state changes: show snackbars and auto-retry on reconnection
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
            showRestoredBanner = false
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "কোনো ইন্টারনেট সংযোগ নেই • অফলাইন মোডে আছেন",
                actionLabel = "ঠিক আছে",
                duration = SnackbarDuration.Short
            )
        } else {
            hasError = false
            errorMsg = ""
            if (wasOffline) {
                wasOffline = false
                showRestoredBanner = true

                webViewInstance?.let { wv ->
                    if (wv.url.isNullOrBlank()) {
                        wv.loadUrl(currentUrl)
                    } else {
                        wv.reload()
                    }
                }

                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = "ইন্টারনেট সংযোগ ফিরে এসেছে!",
                    duration = SnackbarDuration.Short
                )

                delay(3000)
                showRestoredBanner = false
            }
        }
    }

    // Intercept back button to navigate back through web history if available
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                NewsTopAppBar(
                    title = pageTitle,
                    url = currentUrl,
                    progress = progress,
                    isLoading = isLoading,
                    isBookmarked = isBookmarked,
                    onBookmarkToggle = {
                        if (isBookmarked) {
                            preferences.removeBookmark(currentUrl)
                            scope.launch {
                                snackbarHostState.showSnackbar("সংবাদ সংরক্ষণ থেকে সরানো হয়েছে")
                            }
                        } else {
                            preferences.addBookmark(
                                title = if (pageTitle.isNotBlank()) pageTitle else "বাংলা টুডে সংবাদ",
                                url = currentUrl
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("সংবাদ সফলভাবে সংরক্ষিত হয়েছে!")
                            }
                        }
                    },
                    onShare = {
                        shareArticle(context, pageTitle, currentUrl)
                    },
                    onRefresh = {
                        hasError = false
                        isLoading = true
                        progress = 20
                        if (webViewInstance?.url.isNullOrBlank()) {
                            webViewInstance?.loadUrl(currentUrl)
                        } else {
                            webViewInstance?.reload()
                        }
                    },
                    onOpenExternal = {
                        openInBrowser(context, currentUrl)
                    }
                )

                CategoryBar(
                    selectedCategoryUrl = currentUrl,
                    onCategorySelected = { category ->
                        hasError = false
                        currentUrl = category.url
                        isLoading = true
                        progress = 25
                        webViewInstance?.loadUrl(category.url)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated Network Status Banner: Offline warning
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "কোনো ইন্টারনেট সংযোগ নেই • অফলাইন মোড",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Animated Network Status Banner: Restored confirmation
            AnimatedVisibility(
                visible = showRestoredBanner,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = Color(0xFF00796B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ইন্টারনেট সংযোগ ফিরে এসেছে",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                NewsWebView(
                    url = currentUrl,
                    onWebViewCreated = { wv ->
                        webViewInstance = wv
                    },
                    onProgressChanged = { newProgress ->
                        progress = newProgress
                        isLoading = newProgress < 100
                    },
                    onTitleChanged = { title ->
                        if (title.isNotBlank()) {
                            pageTitle = title
                        }
                    },
                    onUrlChanged = { newUrl ->
                        currentUrl = newUrl
                    },
                    onError = { err, msg ->
                        hasError = err
                        errorMsg = msg
                    }
                )

                // If error occurs and device is offline, show friendly OfflineErrorView overlay
                if (hasError && !isOnline) {
                    OfflineErrorView(
                        errorMsg = errorMsg,
                        onRetry = {
                            if (isOnline) {
                                hasError = false
                                webViewInstance?.reload()
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("দয়া করে ইন্টারনেট সংযোগ চালু করুন")
                                }
                            }
                        },
                        onOpenBookmarks = onNavigateToBookmarks,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dynamic Floating Scroll-to-Top button
                FloatingActionButton(
                    onClick = {
                        webViewInstance?.scrollTo(0, 0)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                        .size(36.dp),
                    containerColor = colorResource(R.color.news_primary).copy(alpha = 0.85f),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "শীর্ষে যান",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun shareArticle(context: Context, title: String, url: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\nবিস্তারিত পড়ুন বাংলা টুডে ২৪-এ:\n$url")
        }
        context.startActivity(Intent.createChooser(shareIntent, "সংবাদ শেয়ার করুন"))
    } catch (e: Exception) {
        // Fallback
    }
}

private fun openInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}
