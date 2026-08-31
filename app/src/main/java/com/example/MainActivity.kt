package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TopUpViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TopUpViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FireRed, FireOrange)
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FREE FIRE",
                                color = LightText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOP UP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Diamond,
                                contentDescription = "Diamonds",
                                tint = DiamondCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiamondCyanLight
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = LightText
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = viewModel.currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.currentTab = tab },
                        icon = {
                            val icon = when (tab) {
                                AppNavTab.STORE -> if (isSelected) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart
                                AppNavTab.SPIN_DEALS -> if (isSelected) Icons.Filled.Stars else Icons.Outlined.Stars
                                AppNavTab.HISTORY -> if (isSelected) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong
                                AppNavTab.ACCOUNTS -> if (isSelected) Icons.Filled.People else Icons.Outlined.People
                                AppNavTab.CALCULATOR -> if (isSelected) Icons.Filled.Calculate else Icons.Outlined.Calculate
                            }
                            Icon(
                                icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FireOrangeLight,
                            selectedTextColor = FireOrangeLight,
                            indicatorColor = FireOrange.copy(alpha = 0.2f),
                            unselectedIconColor = SecondaryText,
                            unselectedTextColor = SecondaryText
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = viewModel.currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppNavTab.STORE -> StoreScreen(viewModel = viewModel)
                    AppNavTab.SPIN_DEALS -> SpinDealsScreen(viewModel = viewModel)
                    AppNavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                    AppNavTab.ACCOUNTS -> AccountsScreen(viewModel = viewModel)
                    AppNavTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Payment Bottom Sheet
    if (viewModel.showPaymentSheet) {
        PaymentSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showPaymentSheet = false }
        )
    }

    // Order Success Digital Receipt Dialog
    if (viewModel.showReceiptDialog && viewModel.completedOrder != null) {
        ReceiptDialog(
            order = viewModel.completedOrder!!,
            onDismiss = {
                viewModel.showReceiptDialog = false
                viewModel.completedOrder = null
            }
        )
    }
}
