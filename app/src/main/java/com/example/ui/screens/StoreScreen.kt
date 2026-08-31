package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ServerRegion
import com.example.data.model.TopUpCategory
import com.example.data.model.TopUpItem
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerifiedPlayerCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TopUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: TopUpViewModel,
    modifier: Modifier = Modifier
) {
    val savedAccounts by viewModel.savedAccounts.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    var showRegionMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Full Width Header Banner
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                StoreHeroBanner()
            }

            // Quick Saved Accounts Bar
            if (savedAccounts.isNotEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Column {
                        SectionHeader(
                            title = "Quick Select Profile",
                            badge = "${savedAccounts.size} SAVED"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(savedAccounts) { acc ->
                                val isSelected = viewModel.playerIdInput == acc.playerId
                                Surface(
                                    onClick = { viewModel.selectSavedAccount(acc) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FireOrange.copy(alpha = 0.2f) else DarkSurfaceCard,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FireOrange else DarkSurfaceBorder
                                    ),
                                    modifier = Modifier.testTag("saved_acc_${acc.playerId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (acc.isFavorite) "⭐ " else "🎮 ",
                                            fontSize = 14.sp
                                        )
                                        Column {
                                            Text(
                                                text = acc.nickname,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) FireOrangeLight else LightText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${acc.serverRegion} • ${acc.playerId}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SecondaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Player ID Input Section
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    SectionHeader(
                        title = "1. Enter Player ID & Region",
                        subtitle = "Instant delivery to in-game account"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Region Selector Dropdown Button
                        Box(modifier = Modifier.weight(0.42f)) {
                            Surface(
                                onClick = { showRegionMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceCard,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("region_dropdown_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(viewModel.selectedRegion.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = viewModel.selectedRegion.code,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LightText,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Region",
                                        tint = SecondaryText
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showRegionMenu,
                                onDismissRequest = { showRegionMenu = false },
                                modifier = Modifier.background(DarkSurfaceCard)
                            ) {
                                ServerRegion.entries.forEach { region ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(region.flag, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    region.displayName,
                                                    color = if (region == viewModel.selectedRegion) FireOrangeLight else LightText,
                                                    fontWeight = if (region == viewModel.selectedRegion) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectedRegion = region
                                            showRegionMenu = false
                                            if (viewModel.playerIdInput.isNotBlank()) {
                                                viewModel.verifyPlayer()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Player ID Input Field
                        OutlinedTextField(
                            value = viewModel.playerIdInput,
                            onValueChange = { viewModel.onPlayerIdChanged(it) },
                            placeholder = { Text("Player UID (e.g. 2849104829)", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(0.58f)
                                .testTag("player_id_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.verifyPlayer()
                            }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FireOrange,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            ),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (viewModel.playerIdInput.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onPlayerIdChanged("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SecondaryText)
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text
                                                if (!clip.isNullOrBlank()) {
                                                    viewModel.onPlayerIdChanged(clip.trim())
                                                }
                                            },
                                            modifier = Modifier.testTag("paste_id_btn")
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = FireOrangeLight)
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // Verified Player Card or Progress
                    AnimatedVisibility(
                        visible = viewModel.verifiedPlayer != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        viewModel.verifiedPlayer?.let { player ->
                            Spacer(modifier = Modifier.height(10.dp))
                            VerifiedPlayerCard(
                                player = player,
                                onSaveToFavorites = { viewModel.saveCurrentAccount() }
                            )
                        }
                    }
                }
            }

            // Top-Up Category Filter Tabs
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    SectionHeader(
                        title = "2. Select Top-Up Category",
                        subtitle = "Choose from instant diamonds, memberships & bundles"
                    )
                    CategoryPillSelector(
                        selectedCategory = viewModel.selectedCategory,
                        onCategorySelected = {
                            viewModel.selectedCategory = it
                            viewModel.selectedPackage = null
                        }
                    )
                }
            }

            // Catalog Items Grid
            items(viewModel.filteredCatalog) { item ->
                TopUpItemCard(
                    item = item,
                    isSelected = viewModel.selectedPackage?.id == item.id,
                    onClick = { viewModel.selectedPackage = item }
                )
            }
        }

        // Sticky Bottom Checkout Bar
        Surface(
            color = DarkSurface,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (viewModel.selectedPackage != null) viewModel.selectedPackage!!.name else "Select a Package",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (viewModel.selectedPackage != null) "$${viewModel.selectedPackage!!.price}" else "$0.00",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = FireOrangeLight
                        )
                        if (viewModel.selectedPackage?.bonusDiamonds ?: 0 > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(+${viewModel.selectedPackage!!.bonusDiamonds}💎 free)",
                                fontSize = 11.sp,
                                color = DiamondCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.initiateCheckout() },
                    enabled = viewModel.selectedPackage != null && viewModel.playerIdInput.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FireOrange,
                        disabledContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("checkout_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (viewModel.selectedPackage != null && viewModel.playerIdInput.isNotBlank()) LightText else MutedText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TOP UP NOW",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B2500),
                        Color(0xFF3E1200),
                        Color(0xFF14161F)
                    )
                )
            )
            .border(1.dp, FireOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FireYellow)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFICIAL PARTNER",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ INSTANT 5s DELIVERY",
                        color = FireOrangeLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Free Fire Diamond Top-Up",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = LightText
                )

                Text(
                    text = "Up to 100% Double Diamond Bonus on 1st Top-Up!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(DiamondCyan.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Diamond,
                    contentDescription = "Diamonds",
                    tint = DiamondCyan,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryPillSelector(
    selectedCategory: TopUpCategory,
    onCategorySelected: (TopUpCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(TopUpCategory.entries) { category ->
            val isSelected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) FireOrange else DarkSurfaceCard,
                border = BorderStroke(1.dp, if (isSelected) FireOrange else DarkSurfaceBorder),
                modifier = Modifier.testTag("category_pill_${category.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (category) {
                        TopUpCategory.DIAMONDS -> Icons.Default.Diamond
                        TopUpCategory.MEMBERSHIPS -> Icons.Default.CardMembership
                        TopUpCategory.SPECIAL_CRATES -> Icons.Default.Inventory2
                        TopUpCategory.LEVEL_UP_PASS -> Icons.Default.TrendingUp
                    }
                    Icon(
                        icon,
                        contentDescription = category.title,
                        tint = if (isSelected) LightText else SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LightText else SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
fun TopUpItemCard(
    item: TopUpItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("package_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FireOrange.copy(alpha = 0.15f) else DarkSurfaceCard
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) FireOrange else DarkSurfaceBorder
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge tag (e.g. 2X FIRST TOP UP)
            if (item.badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.isDoubleBonus) FireRed else FireOrange.copy(alpha = 0.2f))
                        .border(
                            0.8.dp,
                            if (item.isDoubleBonus) FireRed else FireOrange,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LightText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Visual Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isSelected) FireOrange else DiamondCyan).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (item.category) {
                    TopUpCategory.DIAMONDS -> Icons.Default.Diamond
                    TopUpCategory.MEMBERSHIPS -> Icons.Default.WorkspacePremium
                    TopUpCategory.SPECIAL_CRATES -> Icons.Default.Inventory
                    TopUpCategory.LEVEL_UP_PASS -> Icons.Default.AutoAwesome
                }
                Icon(
                    icon,
                    contentDescription = item.name,
                    tint = if (isSelected) FireOrangeLight else DiamondCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Diamond / Item Name
            if (item.diamondCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.diamondCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = LightText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "💎",
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bonus text
            if (item.bonusDiamonds > 0) {
                Text(
                    text = "+${item.bonusDiamonds} Bonus 💎",
                    style = MaterialTheme.typography.labelSmall,
                    color = FireOrangeLight,
                    fontWeight = FontWeight.ExtraBold
                )
            } else if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Pill Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) FireOrange else DarkSurfaceElevated)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.originalPrice > item.price) {
                    Text(
                        text = "$${item.originalPrice}",
                        style = MaterialTheme.typography.labelSmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = if (isSelected) LightText.copy(alpha = 0.7f) else MutedText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "$${item.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) LightText else FireOrangeLight
                )
            }
        }
    }
}
