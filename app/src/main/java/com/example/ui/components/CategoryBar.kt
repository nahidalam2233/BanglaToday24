package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.NewsCategories
import com.example.data.model.NewsCategory

@Composable
fun CategoryBar(
    selectedCategoryUrl: String,
    onCategorySelected: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val brandRed = colorResource(R.color.news_primary)

    // Auto-scroll to selected category
    val selectedIndex = NewsCategories.list.indexOfFirst { it.url == selectedCategoryUrl }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex > 0) {
            val targetScroll = (selectedIndex * 80).coerceAtMost(scrollState.maxValue)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        // Ultra-slim category bar: 34dp total height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NewsCategories.list.forEach { category ->
                val isSelected = selectedCategoryUrl == category.url

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) brandRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    label = "category_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "category_text"
                )

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .then(
                            if (isSelected) {
                                Modifier
                            } else {
                                Modifier.border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onCategorySelected(category)
                        }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.nameBn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier.testTag("category_chip_${category.id}")
                    )
                }
            }
        }
    }
}
