package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromoCode
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TopUpViewModel

@Composable
fun SpinDealsScreen(
    viewModel: TopUpViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val animatedRotation by animateFloatAsState(
        targetValue = viewModel.spinRotation,
        animationSpec = tween(durationMillis = 2500),
        label = "wheel_rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Check-In Streak
        item {
            DailyCheckInCard(
                currentStreak = viewModel.dailyCheckInDays,
                claimedToday = viewModel.claimedToday,
                onClaim = { viewModel.claimDailyCheckIn() }
            )
        }

        // Lucky Spin Wheel Section
        item {
            SectionHeader(
                title = "Booyah Lucky Spin Wheel",
                subtitle = "Spin daily for free coupons & diamond multipliers",
                badge = "${viewModel.freeSpinsRemaining} SPINS LEFT"
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, FireOrange.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Interactive Wheel Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animatedRotation)
                        ) {
                            val wheelColors = listOf(
                                Color(0xFFFF5722),
                                Color(0xFF00E5FF),
                                Color(0xFFFF9800),
                                Color(0xFF9C27B0),
                                Color(0xFF4CAF50),
                                Color(0xFFFFC107)
                            )
                            val segmentAngle = 360f / wheelColors.size

                            wheelColors.forEachIndexed { index, color ->
                                drawArc(
                                    color = color,
                                    startAngle = index * segmentAngle,
                                    sweepAngle = segmentAngle,
                                    useCenter = true
                                )
                            }

                            // Outer Ring
                            drawCircle(
                                color = DarkBackground,
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }

                        // Center Hub
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkBackground)
                                .border(2.dp, FireOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Top Pointer
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Pointer",
                            tint = LightText,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(36.dp)
                                .offset(y = (-6).dp)
                        )
                    }

                    if (viewModel.spinPrizeWon != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FireOrange.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, FireOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🎁 ", fontSize = 16.sp)
                                Text(
                                    text = viewModel.spinPrizeWon!!,
                                    color = FireOrangeLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.spinLuckyWheel() },
                        enabled = viewModel.freeSpinsRemaining > 0 && !viewModel.isSpinning,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("spin_wheel_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isSpinning) "SPINNING..." else "SPIN LUCKY WHEEL (${viewModel.freeSpinsRemaining} Left)",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Promo Codes List
        item {
            SectionHeader(
                title = "Available Discount Coupons",
                subtitle = "Tap to copy or apply directly on your next top-up"
            )
        }

        items(listOf(
            PromoCode("BOOYAH50", 50, 0.0, 2.0, "50% OFF on orders over $2.00 (Max $5 discount)"),
            PromoCode("FREEFIRE10", 10, 0.0, 0.5, "10% Instant Discount on any diamond pack"),
            PromoCode("DIAMOND20", 20, 0.0, 4.0, "20% OFF on high-tier diamond packages"),
            PromoCode("NEWBIE", 0, 1.0, 2.0, "$1.00 Flat Discount on your top-up")
        )) { promo ->
            PromoCodeCard(
                promo = promo,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(promo.code))
                    viewModel.snackbarMessage = "Copied ${promo.code} to clipboard!"
                },
                onUse = {
                    viewModel.promoCodeInput = promo.code
                    viewModel.currentTab = AppNavTab.STORE
                }
            )
        }
    }
}

@Composable
fun DailyCheckInCard(
    currentStreak: Int,
    claimedToday: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Login Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                    Text(
                        text = "Current Streak: $currentStreak Days 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        color = FireOrangeLight
                    )
                }

                Button(
                    onClick = onClaim,
                    enabled = !claimedToday,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FireOrange,
                        disabledContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("claim_daily_btn")
                ) {
                    Text(
                        text = if (claimedToday) "CLAIMED ✓" else "CLAIM TODAY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (claimedToday) SecondaryText else LightText
                    )
                }
            }

            // 7-day row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..7).forEach { day ->
                    val isCompleted = day <= currentStreak
                    val isCurrent = day == currentStreak + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCompleted) FireOrange.copy(alpha = 0.2f)
                                else if (isCurrent) DarkSurfaceBorder
                                else DarkSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isCompleted) FireOrange else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "D$day",
                                fontSize = 10.sp,
                                color = if (isCompleted) FireOrangeLight else SecondaryText,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (day == 7) "🎁" else if (isCompleted) "✓" else "💎",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromoCodeCard(
    promo: PromoCode,
    onCopy: () -> Unit,
    onUse: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FireOrange.copy(alpha = 0.15f))
                            .border(1.dp, FireOrange, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = promo.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = FireOrangeLight
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = promo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = onUse,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "USE CODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FireOrangeLight
                )
            }
        }
    }
}
