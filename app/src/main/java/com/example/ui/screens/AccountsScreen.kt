package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavedAccountEntity
import com.example.data.model.ServerRegion
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TopUpViewModel

@Composable
fun AccountsScreen(
    viewModel: TopUpViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.savedAccounts.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "Saved Player IDs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = LightText
                )
                Text(
                    text = "Fast 1-tap top up without typing UID each time",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FireOrange)
                    .testTag("add_account_btn")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Profile",
                    tint = LightText
                )
            }
        }

        if (accounts.isEmpty()) {
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
                            Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "No Saved Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )

                    Text(
                        text = "Save your main account, guild members, or friends' IDs for fast top-ups.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange)
                    ) {
                        Text("Add New Player ID", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(accounts, key = { it.id }) { acc ->
                    SavedAccountCard(
                        account = acc,
                        onSelectForTopUp = {
                            viewModel.selectSavedAccount(acc)
                            viewModel.currentTab = AppNavTab.STORE
                        },
                        onDelete = { viewModel.deleteSavedAccount(acc) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onSave = { uid, nickname, region, rank, level ->
                val entity = SavedAccountEntity(
                    playerId = uid,
                    nickname = nickname,
                    serverRegion = region.code,
                    rankBadge = rank,
                    level = level,
                    isFavorite = true
                )
                viewModel.selectSavedAccount(entity)
                viewModel.saveCurrentAccount()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SavedAccountCard(
    account: SavedAccountEntity,
    onSelectForTopUp: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelectForTopUp() }
            .testTag("account_item_${account.playerId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FireOrange.copy(alpha = 0.2f))
                        .border(1.5.dp, FireOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (account.isFavorite) "👑" else "🎮",
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = DiamondCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "UID: ${account.playerId}",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                        Text("•", color = MutedText, fontSize = 12.sp)
                        Text(
                            text = account.serverRegion,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FireOrangeLight
                        )
                        Text("•", color = MutedText, fontSize = 12.sp)
                        Text(
                            text = "Lv. ${account.level}",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onSelectForTopUp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("topup_acc_btn_${account.playerId}")
                ) {
                    Text("TOP UP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, ServerRegion, String, Int) -> Unit
) {
    var uid by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf(ServerRegion.GLOBAL) }
    var level by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Player Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = { Text("Player UID (8-12 digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("In-Game Nickname") },
                    placeholder = { Text("e.g. ⚡VIPER_FF⚡") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (uid.isNotBlank()) {
                        val name = if (nickname.isNotBlank()) nickname else "Player $uid"
                        val lvl = level.toIntOrNull() ?: 50
                        onSave(uid, name, selectedRegion, "Heroic", lvl)
                    }
                },
                enabled = uid.isNotBlank()
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
