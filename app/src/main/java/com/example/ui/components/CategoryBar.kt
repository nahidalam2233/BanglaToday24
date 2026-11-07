package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsCategories
import com.example.data.model.NewsCategory

@Composable
fun CategoryBar(
    selectedCategoryUrl: String,
    onCategorySelected: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Vibrant Red Gradient for Active Highlight
    val activeGradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE53935), // Vibrant Crimson
                Color(0xFFC62828)  // Deep Brand Red
            )
        )
    }

    // Auto-scroll to selected category smoothly
    val selectedIndex = NewsCategories.list.indexOfFirst { it.url == selectedCategoryUrl }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            val targetScroll = (selectedIndex * 85).coerceAtMost(scrollState.maxValue)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        // Ultra-slim category bar: 34dp height with dynamic highlighted chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NewsCategories.list.forEach { category ->
                val isSelected = selectedCategoryUrl == category.url

                // Dynamic scale and transition animation
                val chipScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.03f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "chip_scale"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "category_text"
                )

                Box(
                    modifier = Modifier
                        .scale(chipScale)
                        .height(28.dp)
                        .then(
                            if (isSelected) {
                                Modifier.shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = Color(0xFFC62828).copy(alpha = 0.35f),
                                    spotColor = Color(0xFFE53935).copy(alpha = 0.45f)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(activeGradient)
                                    .border(
                                        width = 0.75.dp,
                                        color = Color(0xFFFFD54F).copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            } else {
                                Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
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
                        .padding(horizontal = if (isSelected) 11.dp else 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Glowing live bullet indicator for active category
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD54F))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }

                        Text(
                            text = category.nameBn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = if (isSelected) 12.sp else 11.5.sp,
                                letterSpacing = if (isSelected) 0.1.sp else 0.sp
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
}
