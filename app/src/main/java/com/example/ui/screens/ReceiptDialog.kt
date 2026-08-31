package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.OrderEntity
import com.example.ui.theme.*
import com.example.util.QRCodeGenerator
import com.example.util.ReceiptPrinterHelper
import java.text.SimpleDateFormat
import java.util.*

enum class ReceiptViewMode {
    DIGITAL_SUMMARY,
    THERMAL_POS_TAPE,
    VERIFICATION_QR
}

@Composable
fun ReceiptDialog(
    order: OrderEntity,
    autoPrintEnabled: Boolean = true,
    onAutoPrintToggle: ((Boolean) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var viewMode by remember { mutableStateOf(ReceiptViewMode.DIGITAL_SUMMARY) }
    var selectedLayout by remember { mutableStateOf(ReceiptPrinterHelper.PrintLayout.THERMAL_RECEIPT_POS) }
    var isAutoPrintActive by remember { mutableStateOf(autoPrintEnabled) }
    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))

    // Generate QR Code bitmap for this order
    val qrPayload = remember(order.orderId) {
        ReceiptPrinterHelper.generateReceiptVerificationString(order)
    }
    val qrBitmap = remember(qrPayload) {
        QRCodeGenerator.generateQrBitmap(
            content = qrPayload,
            size = 400,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        ).asImageBitmap()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Top-Up Completed!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = LightText
                            )
                            Text(
                                text = "Order: ${order.orderId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs (Digital Summary, Thermal Tape, QR Code)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = viewMode == ReceiptViewMode.DIGITAL_SUMMARY,
                        onClick = { viewMode = ReceiptViewMode.DIGITAL_SUMMARY },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = FireOrange.copy(alpha = 0.25f),
                            activeContentColor = FireOrangeLight,
                            inactiveContainerColor = DarkSurfaceCard,
                            inactiveContentColor = SecondaryText
                        )
                    ) {
                        Text("Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = viewMode == ReceiptViewMode.THERMAL_POS_TAPE,
                        onClick = { viewMode = ReceiptViewMode.THERMAL_POS_TAPE },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = FireOrange.copy(alpha = 0.25f),
                            activeContentColor = FireOrangeLight,
                            inactiveContainerColor = DarkSurfaceCard,
                            inactiveContentColor = SecondaryText
                        )
                    ) {
                        Text("📜 POS Tape", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = viewMode == ReceiptViewMode.VERIFICATION_QR,
                        onClick = { viewMode = ReceiptViewMode.VERIFICATION_QR },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = DiamondCyan.copy(alpha = 0.25f),
                            activeContentColor = DiamondCyanLight,
                            inactiveContainerColor = DarkSurfaceCard,
                            inactiveContentColor = SecondaryText
                        )
                    ) {
                        Text("📱 QR Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (viewMode) {
                        ReceiptViewMode.DIGITAL_SUMMARY -> {
                            DigitalSummaryView(
                                order = order,
                                dateStr = dateStr,
                                qrBitmap = qrBitmap,
                                onCopyTxn = {
                                    clipboardManager.setText(AnnotatedString(order.transactionRef))
                                }
                            )
                        }
                        ReceiptViewMode.THERMAL_POS_TAPE -> {
                            ThermalTapeReceiptView(
                                order = order,
                                dateStr = dateStr,
                                qrBitmap = qrBitmap
                            )
                        }
                        ReceiptViewMode.VERIFICATION_QR -> {
                            QrVerificationCardView(
                                order = order,
                                qrBitmap = qrBitmap,
                                qrPayload = qrPayload,
                                onCopyPayload = {
                                    clipboardManager.setText(AnnotatedString(qrPayload))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Print Toggle & Layout Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceCard,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = FireOrangeLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Auto-Print on Top-Up", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightText)
                                Text(
                                    text = if (isAutoPrintActive) "Prints receipt automatically" else "Manual printing mode",
                                    fontSize = 10.sp,
                                    color = SecondaryText
                                )
                            }
                        }

                        Switch(
                            checked = isAutoPrintActive,
                            onCheckedChange = { checked ->
                                isAutoPrintActive = checked
                                onAutoPrintToggle?.invoke(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FireOrange,
                                uncheckedThumbColor = SecondaryText,
                                uncheckedTrackColor = DarkSurfaceBorder
                            ),
                            modifier = Modifier.testTag("auto_print_toggle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row (Print Receipt & Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { ReceiptPrinterHelper.shareReceiptText(context, order) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_receipt_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            ReceiptPrinterHelper.printOrderReceipt(
                                context = context,
                                order = order,
                                layout = selectedLayout
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier
                            .weight(1.8f)
                            .height(48.dp)
                            .testTag("print_receipt_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Receipt 🖨️", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalSummaryView(
    order: OrderEntity,
    dateStr: String,
    qrBitmap: androidx.compose.ui.graphics.ImageBitmap,
    onCopyTxn: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Zero-Fee Capped Guarantee Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = EmeraldGreen.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "100% DIRECT FREE FIRE DELIVERY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Zero Fee Capped ($0.00 Surcharge) • Official Partner",
                        fontSize = 10.sp,
                        color = LightText
                    )
                }
            }
        }

        // Details Container
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkSurfaceCard,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptRow(label = "Order ID", value = order.orderId)
                ReceiptRow(label = "Transaction Ref", value = order.transactionRef, isCopyable = true, onCopy = onCopyTxn)
                ReceiptRow(label = "Player Nickname", value = order.playerNickname)
                ReceiptRow(label = "Player UID", value = order.playerId)
                ReceiptRow(label = "Server Region", value = order.serverRegion)
                ReceiptRow(label = "Item Purchased", value = order.itemName)

                if (order.diamondCount > 0) {
                    ReceiptRow(
                        label = "Diamonds Delivered",
                        value = "${order.diamondCount + order.bonusDiamonds} 💎 (${order.diamondCount} + ${order.bonusDiamonds} Bonus)"
                    )
                }

                ReceiptRow(label = "Payment Channel", value = order.paymentMethod)
                ReceiptRow(label = "Processing Fee", value = "$0.00 (Zero Capped ⚡)")
                if (order.discountAmount > 0) {
                    ReceiptRow(label = "Promo Savings", value = "-$${order.discountAmount} (${order.promoCodeApplied ?: ""})")
                }
                ReceiptRow(label = "Date & Time", value = dateStr)

                HorizontalDivider(color = DarkSurfaceBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Paid",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText
                    )
                    Text(
                        text = "$${order.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = FireOrangeLight
                    )
                }
            }
        }

        // Compact embedded QR preview
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkSurfaceCard,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Order QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Embedded Verification QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightText)
                    Text(
                        "Includes tamper-proof cryptographic transaction metadata for direct printer outputs.",
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun ThermalTapeReceiptView(
    order: OrderEntity,
    dateStr: String,
    qrBitmap: androidx.compose.ui.graphics.ImageBitmap
) {
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFAF9F6),
        border = BorderStroke(1.dp, Color(0xFFD6D6D6)),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Jagged Top Thermal indicator
            Text(
                text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🔥 FREE FIRE TOP-UP 🔥",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
            Text(
                text = "OFFICIAL PARTNER RECEIPT",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.DarkGray
            )
            Text(
                text = "ZERO PROCESSING FEE GUARANTEE",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "------------------------------------------",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThermalRow("ORDER ID:", order.orderId)
                ThermalRow("TXN REF:", order.transactionRef)
                ThermalRow("DATE:", dateStr)
                ThermalRow("UID:", order.playerId)
                ThermalRow("NICKNAME:", order.playerNickname)
                ThermalRow("REGION:", order.serverRegion)
                ThermalRow("ITEM:", order.itemName)
                if (order.diamondCount > 0) {
                    ThermalRow("DIAMONDS:", "${order.diamondCount + order.bonusDiamonds} 💎")
                }
                ThermalRow("GATEWAY:", order.paymentMethod)
                ThermalRow("FEE:", "$0.00 (0% CAPPED)")
                if (order.discountAmount > 0) {
                    ThermalRow("PROMO:", "-$${order.discountAmount}")
                }
            }

            Text(
                text = "==========================================",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL PAID:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "$${order.price}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFE65100)
                )
            }

            Text(
                text = "------------------------------------------",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Thermal printed QR
            Surface(
                color = Color.White,
                modifier = Modifier.size(130.dp)
            ) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = "Receipt QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SCAN QR TO VERIFY TRANSACTION",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = Color.DarkGray
            )
            Text(
                text = "THANK YOU FOR PLAYING FREE FIRE!",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• • • • • • • • • • • • • • • • • • • • • • • • • • • •",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ThermalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.DarkGray
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun QrVerificationCardView(
    order: OrderEntity,
    qrBitmap: androidx.compose.ui.graphics.ImageBitmap,
    qrPayload: String,
    onCopyPayload: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Official Free Fire Receipt Pass",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = DiamondCyanLight
        )

        // Large high-res QR Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = "Full QR Pass",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = order.orderId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Player: ${order.playerNickname} • $${order.price}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        // Copy QR Payload Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurfaceCard,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Raw QR Verification Payload", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightText)
                    Text(
                        text = qrPayload,
                        fontSize = 9.sp,
                        color = SecondaryText,
                        maxLines = 2
                    )
                }
                IconButton(onClick = onCopyPayload) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DiamondCyanLight)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    isCopyable: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SecondaryText
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            if (isCopyable && onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = FireOrangeLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
