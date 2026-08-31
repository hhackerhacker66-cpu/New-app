package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.OrderEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TopUpViewModel
import com.example.util.ReceiptPrinterHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: TopUpViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var selectedOrderForReceipt by remember { mutableStateOf<OrderEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showScanVerifyInput by remember { mutableStateOf(false) }
    var manualQrInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Top-Up History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = LightText
                )
                Text(
                    text = "${orders.size} Total Orders • Auto-Print & QR Enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showScanVerifyInput = true },
                    modifier = Modifier.testTag("verify_qr_btn")
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Verify QR",
                        tint = DiamondCyanLight
                    )
                }

                if (orders.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = ErrorRed
                        )
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "No Top-Up Orders Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )

                    Text(
                        text = "Your completed diamond top-ups will be saved and tracked here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.currentTab = AppNavTab.STORE },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier.testTag("go_to_store_btn")
                    ) {
                        Text("Top-Up Diamonds Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.id }) { order ->
                    HistoryOrderCard(
                        order = order,
                        onClick = { selectedOrderForReceipt = order },
                        onPrint = {
                            ReceiptPrinterHelper.printOrderReceipt(
                                context = context,
                                order = order,
                                layout = viewModel.selectedPrintLayout
                            )
                        },
                        onDelete = { viewModel.deleteOrder(order) },
                        onReOrder = {
                            viewModel.playerIdInput = order.playerId
                            viewModel.currentTab = AppNavTab.STORE
                        }
                    )
                }
            }
        }
    }

    selectedOrderForReceipt?.let { order ->
        ReceiptDialog(
            order = order,
            autoPrintEnabled = viewModel.autoPrintReceipt,
            onAutoPrintToggle = { viewModel.autoPrintReceipt = it },
            onDismiss = { selectedOrderForReceipt = null }
        )
    }

    // QR Verification Dialog
    if (showScanVerifyInput) {
        AlertDialog(
            onDismissRequest = { showScanVerifyInput = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = DiamondCyanLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Receipt QR Payload")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste or simulate scan of a Free Fire top-up receipt QR string to verify authenticity & zero fee cap.",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                    OutlinedTextField(
                        value = manualQrInput,
                        onValueChange = { manualQrInput = it },
                        placeholder = { Text("FF-RECEIPT|OID:...|UID:...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showScanVerifyInput = false
                        viewModel.verifyQrPayload(manualQrInput)
                        manualQrInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan)
                ) {
                    Text("Verify Authenticity", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanVerifyInput = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Verification Result Modal
    if (viewModel.showQrVerificationModal) {
        AlertDialog(
            onDismissRequest = { viewModel.showQrVerificationModal = false },
            title = {
                Text(
                    text = "Receipt QR Verification",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = viewModel.verificationResultText ?: "No data verified",
                    fontSize = 13.sp,
                    color = LightText
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.showQrVerificationModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FireOrange)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Order History?") },
            text = { Text("This will remove all transaction records stored locally on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllOrders()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear All", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryOrderCard(
    order: OrderEntity,
    onClick: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
    onReOrder: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("history_card_${order.orderId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DELIVERED ⚡",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }

                Text(
                    text = "$${order.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = FireOrangeLight
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DiamondCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Diamond,
                        contentDescription = null,
                        tint = DiamondCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.itemName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                    Text(
                        text = "To: ${order.playerNickname} (${order.playerId})",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrint,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print Receipt",
                            tint = FireOrangeLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onReOrder,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Top Up Again",
                            tint = DiamondCyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = SecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
