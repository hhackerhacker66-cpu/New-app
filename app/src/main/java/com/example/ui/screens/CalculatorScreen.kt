package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TopUpViewModel

@Composable
fun CalculatorScreen(
    viewModel: TopUpViewModel,
    modifier: Modifier = Modifier
) {
    val (diamondsNeeded, approxPrice) = viewModel.calculateEvoCost()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Free Fire Diamond Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = LightText
                )
                Text(
                    text = "Plan diamond requirements for Evo Guns, Events & Mystery Shop",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        // Evo Gun Selection
        item {
            SectionHeader(
                title = "1. Select Evo Gun Skin",
                subtitle = "Choose gun to upgrade from Lv 1 to Lv 7"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(viewModel.evoGuns) { gun ->
                    val isSelected = viewModel.selectedEvoGun == gun
                    Surface(
                        onClick = { viewModel.selectedEvoGun = gun },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) FireOrange.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) FireOrange else DarkSurfaceBorder),
                        modifier = Modifier.testTag("evo_gun_${gun.name.take(4)}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(gun.iconEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gun.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FireOrangeLight else LightText
                            )
                        }
                    }
                }
            }
        }

        // Level Range Selectors
        item {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Upgrade Range (Level ${viewModel.currentEvoLevel} ➔ Level ${viewModel.targetEvoLevel})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )

                    // Current Level
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Level: Lv. ${viewModel.currentEvoLevel}", color = SecondaryText, fontSize = 13.sp)
                        }
                        Slider(
                            value = viewModel.currentEvoLevel.toFloat(),
                            onValueChange = {
                                viewModel.currentEvoLevel = it.toInt()
                                if (viewModel.targetEvoLevel <= viewModel.currentEvoLevel) {
                                    viewModel.targetEvoLevel = (viewModel.currentEvoLevel + 1).coerceAtMost(7)
                                }
                            },
                            valueRange = 1f..6f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = DiamondCyan,
                                activeTrackColor = DiamondCyan,
                                inactiveTrackColor = DarkSurfaceBorder
                            )
                        )
                    }

                    // Target Level
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Level: Lv. ${viewModel.targetEvoLevel}", color = SecondaryText, fontSize = 13.sp)
                        }
                        Slider(
                            value = viewModel.targetEvoLevel.toFloat(),
                            onValueChange = {
                                viewModel.targetEvoLevel = it.toInt().coerceAtLeast(viewModel.currentEvoLevel + 1)
                            },
                            valueRange = 2f..7f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = FireOrange,
                                activeTrackColor = FireOrange,
                                inactiveTrackColor = DarkSurfaceBorder
                            )
                        )
                    }
                }
            }
        }

        // Calculation Result Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.5.dp, FireOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estimated Requirement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$diamondsNeeded",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = DiamondCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("💎 Diamonds", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
                    }

                    Text(
                        text = "Approx. Top-Up Cost: $$approxPrice",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = FireOrangeLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            // Suggest best matching pack in Store
                            val bestPack = viewModel.catalogItems.find { it.diamondCount >= diamondsNeeded }
                                ?: viewModel.catalogItems.last()
                            viewModel.selectedPackage = bestPack
                            viewModel.currentTab = AppNavTab.STORE
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("buy_matching_pack_btn")
                    ) {
                        Text("Buy Diamonds for this Upgrade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Event Quick Estimator
        item {
            SectionHeader(
                title = "Popular Event Diamond Cost Guide",
                subtitle = "Average diamond budget required based on probability"
            )
        }

        items(listOf(
            Triple("Mystery Shop 90% Discount", "500 - 1,200 💎", "Elite Pass + Bundles"),
            Triple("Incubator 3 Blueprints + 7 Stones", "2,500 - 4,000 💎", "Top Tier Weapon / Costume"),
            Triple("Faded Wheel (Guaranteed 8 Spins)", "1,082 💎", "New Emote / Gun Skin"),
            Triple("Hyperbook Full Unlock (All Pages)", "3,500 💎", "All Exclusive Animations")
        )) { (title, diamonds, desc) ->
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diamonds,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = FireOrangeLight
                        )
                    }
                }
            }
        }
    }
}
