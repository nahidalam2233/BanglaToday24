package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun NewsTopAppBar(
    title: String,
    url: String,
    progress: Int,
    isLoading: Boolean,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    onOpenExternal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val brandRed = colorResource(R.color.news_primary)

    Surface(
        color = brandRed,
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Ultra-slim header row: exact 42dp height (~85px)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Brand Logo (26dp)
                Image(
                    painter = painterResource(R.drawable.ic_bangla_today_logo),
                    contentDescription = "বাংলা টুডে ২৪ লোগো",
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Brand Title & live subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "বাংলা টুডে ২৪",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                    if (title.isNotBlank() && title != "Bangla Today 24" && title != "Home") {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Compact action buttons (size 32dp, icon 18dp)
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("top_bar_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "বুকমার্ক",
                        tint = if (isBookmarked) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("top_bar_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "শেয়ার",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("top_bar_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "রিফ্রেশ",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("top_bar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "মেন্যু",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ব্রাউজারে খুলুন") },
                            onClick = {
                                showMenu = false
                                onOpenExternal()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("পেজ রিলোড") },
                            onClick = {
                                showMenu = false
                                onRefresh()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Slim 2.5dp Progress Bar when loading
            AnimatedVisibility(
                visible = isLoading && progress in 1..99,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = Color(0xFFFFD54F),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}
