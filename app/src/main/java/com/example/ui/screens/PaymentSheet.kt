package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentType
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TopUpViewModel
import com.example.util.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSheet(
    viewModel: TopUpViewModel,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val pkg = viewModel.selectedPackage ?: return

    ModalBottomSheet(
        onDismissRequest = {
            if (!viewModel.isProcessingPayment) {
                onDismiss()
            }
        },
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkSurfaceBorder)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Checkout Order",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                        Text(
                            text = "Order will be delivered directly to Free Fire ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FireOrange.copy(alpha = 0.15f))
                            .border(1.dp, FireOrange, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ 5s FAST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FireOrangeLight
                        )
                    }
                }
            }

            // Target Account Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceCard,
                    border = BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FireOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎮", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.verifiedPlayer?.nickname ?: "Player ${viewModel.playerIdInput}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = LightText
                            )
                            Text(
                                text = "UID: ${viewModel.playerIdInput} (${viewModel.selectedRegion.displayName})",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText
                            )
                        }
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Selected Package Overview
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceCard,
                    border = BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Diamond,
                                contentDescription = null,
                                tint = DiamondCyan,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LightText
                                )
                                if (pkg.bonusDiamonds > 0) {
                                    Text(
                                        text = "+${pkg.bonusDiamonds} Bonus Diamonds Included",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FireOrangeLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "$${pkg.price}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                    }
                }
            }

            // Promo Code Section
            item {
                Column {
                    SectionHeader(
                        title = "Apply Promo / Coupon Code",
                        subtitle = "Try BOOYAH50, FREEFIRE10, or NEWBIE"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.promoCodeInput,
                            onValueChange = {
                                viewModel.promoCodeInput = it.uppercase()
                                viewModel.promoError = null
                            },
                            placeholder = { Text("Enter Promo Code", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("promo_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.applyPromoCode()
                            }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FireOrange,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            ),
                            isError = viewModel.promoError != null
                        )

                        if (viewModel.appliedPromoCode != null) {
                            Button(
                                onClick = { viewModel.removePromoCode() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("remove_promo_btn")
                            ) {
                                Text("Remove")
                            }
                        } else {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.applyPromoCode()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("apply_promo_btn")
                            ) {
                                Text("Apply", color = FireOrangeLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (viewModel.promoError != null) {
                        Text(
                            text = viewModel.promoError!!,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    if (viewModel.appliedPromoCode != null) {
                        Row(
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Code ${viewModel.appliedPromoCode!!.code} applied! Saved $${viewModel.calculateDiscountAmount()}",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Payment Methods
            item {
                SectionHeader(
                    title = "Select Payment Gateway (Zero Fee Capped ⚡)",
                    subtitle = "All official channels 100% secure • $0.00 Surcharge"
                )
            }

            items(PaymentType.entries) { paymentType ->
                val isSelected = viewModel.selectedPaymentMethod == paymentType
                Surface(
                    onClick = { viewModel.selectedPaymentMethod = paymentType },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) FireOrange.copy(alpha = 0.12f) else DarkSurfaceCard,
                    border = BorderStroke(1.dp, if (isSelected) FireOrange else DarkSurfaceBorder),
                    modifier = Modifier.testTag("pay_method_${paymentType.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectedPaymentMethod = paymentType },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = FireOrange,
                                    unselectedColor = SecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = paymentType.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = LightText
                                )
                                Text(
                                    text = paymentType.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryText
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("0% FEE", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // If QR_SCAN_PAY is selected, render dynamic payment QR code
            if (viewModel.selectedPaymentMethod == PaymentType.QR_SCAN_PAY) {
                item {
                    val finalPrice = viewModel.calculateFinalPrice()
                    val paymentQrString = "upi://pay?pa=freefire.topup@garena&pn=FreeFireOfficial&am=$finalPrice&cu=USD&tn=FF-TopUp-${viewModel.playerIdInput}"
                    val paymentQrBitmap = remember(finalPrice, viewModel.playerIdInput) {
                        QRCodeGenerator.generateQrBitmap(
                            content = paymentQrString,
                            size = 300,
                            foregroundColor = android.graphics.Color.BLACK,
                            backgroundColor = android.graphics.Color.WHITE
                        ).asImageBitmap()
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurfaceElevated,
                        border = BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📱 Scan QR Code to Pay Instant",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = DiamondCyanLight
                            )
                            Text(
                                text = "Scan with any UPI, Banking app, or Wallet (0% Fee)",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.size(160.dp)
                            ) {
                                Image(
                                    bitmap = paymentQrBitmap,
                                    contentDescription = "Payment QR",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Payable Amount: $$finalPrice (Zero Processing Fee)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = LightText
                            )
                        }
                    }
                }
            }

            // Pricing Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Package Price", color = SecondaryText, fontSize = 14.sp)
                            Text("$${pkg.price}", color = LightText, fontSize = 14.sp)
                        }

                        if (viewModel.appliedPromoCode != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Promo Discount (${viewModel.appliedPromoCode!!.code})", color = EmeraldGreen, fontSize = 14.sp)
                                Text("-$${viewModel.calculateDiscountAmount()}", color = EmeraldGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (viewModel.selectedPaymentMethod.processingFee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gateway Fee", color = SecondaryText, fontSize = 14.sp)
                                Text("+$${viewModel.selectedPaymentMethod.processingFee}", color = LightText, fontSize = 14.sp)
                            }
                        }

                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
                            Text(
                                text = "$${viewModel.calculateFinalPrice()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = FireOrangeLight
                            )
                        }
                    }
                }
            }

            // Pay Action Button & Processing Animation
            item {
                if (viewModel.isProcessingPayment) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = FireOrange,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = viewModel.paymentStepMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FireOrangeLight
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.processOrderPayment() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("confirm_pay_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONFIRM & PAY $${viewModel.calculateFinalPrice()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
