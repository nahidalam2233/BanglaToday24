package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.NewsCategories
import com.example.data.preferences.AppPreferences
import com.example.fcm.NotificationHelper
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebNewsScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainNavTab(
    val labelBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    NEWS("সংবাদ", Icons.Filled.Newspaper, Icons.Outlined.Newspaper, "nav_tab_news"),
    BOOKMARKS("সংরক্ষিত", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_tab_bookmarks"),
    NOTIFICATIONS("বিজ্ঞপ্তি", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_tab_notifications"),
    SETTINGS("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_settings")
}

class MainActivity : ComponentActivity() {

    private val targetUrlState = mutableStateOf(NewsCategories.BASE_URL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Setup notification channel
        NotificationHelper.createNotificationChannel(this)

        // 2. Sync FCM token and subscribe to news topics
        NotificationHelper.syncFcmToken(this)
        NotificationHelper.subscribeTopic("all")
        NotificationHelper.subscribeTopic("breaking")
        NotificationHelper.subscribeTopic("news")

        // 3. Process launch Intent (from notification or deep link)
        extractUrlFromIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    targetUrl = targetUrlState.value,
                    onUrlHandled = { targetUrlState.value = it }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractUrlFromIntent(intent)
    }

    private fun extractUrlFromIntent(intent: Intent?) {
        val extraUrl = intent?.getStringExtra(EXTRA_URL)
        val dataUri = intent?.data?.toString()
        val url = extraUrl ?: dataUri
        if (!url.isNullOrBlank()) {
            targetUrlState.value = url
        }
    }

    companion object {
        const val EXTRA_URL = "extra_news_url"
    }
}

@Composable
fun MainAppScreen(
    targetUrl: String,
    onUrlHandled: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences.getInstance(context) }
    var currentTab by remember { mutableStateOf(MainNavTab.NEWS) }

    val bookmarks by preferences.bookmarks.collectAsState()
    val notifications by preferences.notifications.collectAsState()

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.syncFcmToken(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Switch to news tab if an external URL or notification was passed
    LaunchedEffect(targetUrl) {
        if (targetUrl != NewsCategories.BASE_URL) {
            currentTab = MainNavTab.NEWS
        }
    }

    // Navigate back to NEWS tab if currently on a sub-tab
    BackHandler(enabled = currentTab != MainNavTab.NEWS) {
        currentTab = MainNavTab.NEWS
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            SlimBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                bookmarkCount = bookmarks.size,
                notificationCount = notifications.size,
                modifier = Modifier.testTag("bottom_nav_bar")
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainNavTab.NEWS -> {
                    WebNewsScreen(
                        initialUrl = targetUrl,
                        onNavigateToBookmarks = { currentTab = MainNavTab.BOOKMARKS }
                    )
                }
                MainNavTab.BOOKMARKS -> {
                    BookmarksScreen(
                        onArticleClick = { url ->
                            onUrlHandled(url)
                            currentTab = MainNavTab.NEWS
                        },
                        onNavigateHome = {
                            onUrlHandled(NewsCategories.BASE_URL)
                            currentTab = MainNavTab.NEWS
                        }
                    )
                }
                MainNavTab.NOTIFICATIONS -> {
                    NotificationCenterScreen(
                        onOpenArticleUrl = { url ->
                            onUrlHandled(url)
                            currentTab = MainNavTab.NEWS
                        }
                    )
                }
                MainNavTab.SETTINGS -> {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun SlimBottomBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    bookmarkCount: Int,
    notificationCount: Int,
    modifier: Modifier = Modifier
) {
    val brandRed = colorResource(R.color.news_primary)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(44.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MainNavTab.entries.forEach { tab ->
                val selected = currentTab == tab
                val tint = if (selected) brandRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 1.dp)
                        .testTag(tab.testTag)
                ) {
                    BadgedBox(
                        badge = {
                            if (tab == MainNavTab.BOOKMARKS && bookmarkCount > 0) {
                                Badge(
                                    containerColor = brandRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$bookmarkCount",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                    )
                                }
                            } else if (tab == MainNavTab.NOTIFICATIONS && notificationCount > 0) {
                                Badge(
                                    containerColor = brandRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$notificationCount",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.labelBn,
                            tint = tint,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Text(
                        text = tab.labelBn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = tint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
