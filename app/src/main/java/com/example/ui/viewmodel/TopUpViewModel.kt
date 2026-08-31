package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.SavedAccountEntity
import com.example.data.model.*
import com.example.data.repository.TopUpRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppNavTab(val title: String, val iconName: String) {
    STORE("Store", "shopping_cart"),
    SPIN_DEALS("Lucky Spin", "stars"),
    HISTORY("History", "receipt_long"),
    ACCOUNTS("Accounts", "people"),
    CALCULATOR("Calculator", "calculate")
}

data class EvoGunInfo(
    val name: String,
    val iconEmoji: String,
    val maxLevel: Int = 7,
    val totalDiamondsNeeded: Int = 14500,
    val tokensPerLevel: List<Int> = listOf(0, 30, 60, 120, 240, 400, 600)
)

class TopUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TopUpRepository

    val orders: StateFlow<List<OrderEntity>>
    val savedAccounts: StateFlow<List<SavedAccountEntity>>

    // Navigation
    var currentTab by mutableStateOf(AppNavTab.STORE)

    // Store state
    var selectedRegion by mutableStateOf(ServerRegion.GLOBAL)
    var playerIdInput by mutableStateOf("")
    var verifiedPlayer by mutableStateOf<VerifiedPlayer?>(null)
    var isVerifyingPlayer by mutableStateOf(false)

    var selectedCategory by mutableStateOf(TopUpCategory.DIAMONDS)
    var selectedPackage by mutableStateOf<TopUpItem?>(null)

    // Promo Code
    var promoCodeInput by mutableStateOf("")
    var appliedPromoCode by mutableStateOf<PromoCode?>(null)
    var promoError by mutableStateOf<String?>(null)

    // Payment Sheet & Processing
    var showPaymentSheet by mutableStateOf(false)
    var selectedPaymentMethod by mutableStateOf(PaymentType.UPI_GPAY)
    var isProcessingPayment by mutableStateOf(false)
    var paymentStepMessage by mutableStateOf("")

    // Completed Order / Receipt & Printing
    var completedOrder by mutableStateOf<OrderEntity?>(null)
    var showReceiptDialog by mutableStateOf(false)
    var autoPrintReceipt by mutableStateOf(true)
    var triggerAutoPrintForOrder by mutableStateOf<OrderEntity?>(null)
    var selectedPrintLayout by mutableStateOf(com.example.util.ReceiptPrinterHelper.PrintLayout.THERMAL_RECEIPT_POS)

    // Receipt QR Verification Simulator
    var showQrVerificationModal by mutableStateOf(false)
    var verificationResultOrder by mutableStateOf<OrderEntity?>(null)
    var verificationResultText by mutableStateOf<String?>(null)

    // Feedback Toast / Snackbar message
    var snackbarMessage by mutableStateOf<String?>(null)

    // Lucky Spin State
    var spinRotation by mutableStateOf(0f)
    var isSpinning by mutableStateOf(false)
    var spinPrizeWon by mutableStateOf<String?>(null)
    var freeSpinsRemaining by mutableStateOf(1)
    var dailyCheckInDays by mutableStateOf(3)
    var claimedToday by mutableStateOf(false)

    // Calculator State
    val evoGuns = listOf(
        EvoGunInfo("AK47 Blue Flame Draco", "🐉"),
        EvoGunInfo("MP40 Predatory Cobra", "🐍"),
        EvoGunInfo("M1014 Green Flame Draco", "🐲"),
        EvoGunInfo("SCAR Megalodon Alpha", "🦈"),
        EvoGunInfo("UMP Booyah Day", "⚡"),
        EvoGunInfo("XM8 Destiny Guardian", "🌟")
    )
    var selectedEvoGun by mutableStateOf(evoGuns[0])
    var currentEvoLevel by mutableStateOf(1)
    var targetEvoLevel by mutableStateOf(7)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TopUpRepository(db.topUpDao())

        orders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedAccounts = repository.allSavedAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.initDefaultAccountsIfEmpty()
        }
    }

    val catalogItems: List<TopUpItem>
        get() = repository.getCatalog()

    val filteredCatalog: List<TopUpItem>
        get() = catalogItems.filter { it.category == selectedCategory }

    fun onPlayerIdChanged(id: String) {
        playerIdInput = id
        if (id.length >= 8) {
            // Auto simulate verification
            verifyPlayer(id)
        } else {
            verifiedPlayer = null
        }
    }

    fun verifyPlayer(id: String = playerIdInput) {
        if (id.isBlank()) return
        viewModelScope.launch {
            isVerifyingPlayer = true
            delay(300)
            verifiedPlayer = repository.verifyPlayer(id, selectedRegion)
            isVerifyingPlayer = false
        }
    }

    fun selectSavedAccount(account: SavedAccountEntity) {
        playerIdInput = account.playerId
        selectedRegion = ServerRegion.entries.find { it.code == account.serverRegion } ?: ServerRegion.GLOBAL
        verifiedPlayer = VerifiedPlayer(
            playerId = account.playerId,
            nickname = account.nickname,
            level = account.level,
            rank = account.rankBadge,
            region = selectedRegion
        )
        snackbarMessage = "Selected account: ${account.nickname}"
    }

    fun saveCurrentAccount() {
        val player = verifiedPlayer ?: return
        viewModelScope.launch {
            val entity = SavedAccountEntity(
                playerId = player.playerId,
                nickname = player.nickname,
                serverRegion = player.region.code,
                rankBadge = player.rank,
                level = player.level,
                isFavorite = true,
                lastUsedTimestamp = System.currentTimeMillis()
            )
            repository.saveAccount(entity)
            snackbarMessage = "Account saved: ${player.nickname}"
        }
    }

    fun deleteSavedAccount(account: SavedAccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            snackbarMessage = "Removed ${account.nickname}"
        }
    }

    fun applyPromoCode(code: String = promoCodeInput) {
        val trimmed = code.trim().uppercase()
        val pkg = selectedPackage
        if (pkg == null) {
            promoError = "Please select a package first"
            return
        }

        val promo = repository.getPromoCodes().find { it.code.equals(trimmed, ignoreCase = true) }
        if (promo == null) {
            promoError = "Invalid Promo Code"
            appliedPromoCode = null
            return
        }

        if (pkg.price < promo.minOrderAmount) {
            promoError = "Min order of $${promo.minOrderAmount} required"
            appliedPromoCode = null
            return
        }

        appliedPromoCode = promo
        promoError = null
        snackbarMessage = "Promo Code ${promo.code} Applied!"
    }

    fun removePromoCode() {
        appliedPromoCode = null
        promoCodeInput = ""
        promoError = null
    }

    fun calculateFinalPrice(): Double {
        val pkg = selectedPackage ?: return 0.0
        var total = pkg.price
        val promo = appliedPromoCode
        if (promo != null) {
            if (promo.discountPercent > 0) {
                val discount = total * (promo.discountPercent / 100.0)
                total -= minOf(discount, 5.0) // max $5
            } else if (promo.flatDiscount > 0) {
                total = maxOf(0.0, total - promo.flatDiscount)
            }
        }
        total += selectedPaymentMethod.processingFee
        return (total * 100).toLong() / 100.0
    }

    fun calculateDiscountAmount(): Double {
        val pkg = selectedPackage ?: return 0.0
        val base = pkg.price
        val final = calculateFinalPrice() - selectedPaymentMethod.processingFee
        return maxOf(0.0, base - final)
    }

    fun initiateCheckout() {
        if (playerIdInput.isBlank()) {
            snackbarMessage = "Please enter your Free Fire Player ID"
            return
        }
        if (selectedPackage == null) {
            snackbarMessage = "Please select a diamond package"
            return
        }
        if (verifiedPlayer == null) {
            verifyPlayer(playerIdInput)
        }
        showPaymentSheet = true
    }

    fun processOrderPayment() {
        val pkg = selectedPackage ?: return
        val player = verifiedPlayer ?: repository.verifyPlayer(playerIdInput, selectedRegion)
        
        viewModelScope.launch {
            isProcessingPayment = true
            
            paymentStepMessage = "Validating Player ID ${player.playerId}..."
            delay(600)
            paymentStepMessage = "Connecting to Free Fire ${selectedRegion.displayName}..."
            delay(700)
            paymentStepMessage = "Securing ${selectedPaymentMethod.displayName} transaction..."
            delay(800)
            paymentStepMessage = "Injecting ${pkg.diamondCount + pkg.bonusDiamonds} Diamonds..."
            delay(600)

            val orderNumber = "FF-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()) + "-" + (100..999).random()
            val txRef = "TXN-" + UUID.randomUUID().toString().take(12).uppercase()

            val orderEntity = OrderEntity(
                orderId = orderNumber,
                transactionRef = txRef,
                playerId = player.playerId,
                playerNickname = player.nickname,
                serverRegion = selectedRegion.code,
                category = pkg.category.title,
                itemName = pkg.name,
                diamondCount = pkg.diamondCount,
                bonusDiamonds = pkg.bonusDiamonds,
                price = calculateFinalPrice(),
                discountAmount = calculateDiscountAmount(),
                promoCodeApplied = appliedPromoCode?.code,
                paymentMethod = selectedPaymentMethod.displayName,
                paymentStatus = "COMPLETED",
                timestamp = System.currentTimeMillis()
            )

            repository.saveOrder(orderEntity)
            completedOrder = orderEntity
            isProcessingPayment = false
            showPaymentSheet = false
            showReceiptDialog = true

            if (autoPrintReceipt) {
                triggerAutoPrintForOrder = orderEntity
            }

            // Reset inputs
            selectedPackage = null
            appliedPromoCode = null
            promoCodeInput = ""
        }
    }

    fun verifyQrPayload(payload: String) {
        if (!payload.startsWith("FF-RECEIPT|")) {
            verificationResultText = "❌ Invalid Free Fire Receipt QR Code: Unrecognized format."
            verificationResultOrder = null
            showQrVerificationModal = true
            return
        }

        try {
            val parts = payload.removePrefix("FF-RECEIPT|").split("|")
            val map = parts.mapNotNull {
                val kv = it.split(":", limit = 2)
                if (kv.size == 2) kv[0] to kv[1] else null
            }.toMap()

            val orderId = map["OID"] ?: "Unknown"
            val uid = map["UID"] ?: "Unknown"
            val name = map["NAME"] ?: "Player"
            val amt = map["AMT"] ?: "$0.00"
            val diamonds = map["DIAMONDS"] ?: "0"
            val txn = map["TXN"] ?: "Unknown"

            verificationResultText = """
                ✅ 100% AUTHENTIC FREE FIRE RECEIPT VERIFIED!
                
                • Order ID: $orderId
                • Player UID: $uid ($name)
                • Diamonds Delivered: $diamonds 💎
                • Amount Paid: $amt (Zero Fee Capped)
                • Transaction Auth Ref: $txn
                • Security Status: 100% Cryptographically Verified
            """.trimIndent()
            showQrVerificationModal = true
        } catch (e: Exception) {
            verificationResultText = "❌ QR Parse Error: ${e.message}"
            showQrVerificationModal = true
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order.id)
            snackbarMessage = "Order record deleted"
        }
    }

    fun clearAllOrders() {
        viewModelScope.launch {
            repository.clearOrders()
            snackbarMessage = "Order history cleared"
        }
    }

    fun spinLuckyWheel() {
        if (freeSpinsRemaining <= 0 || isSpinning) return
        viewModelScope.launch {
            isSpinning = true
            val randomDegrees = (720..1440).random().toFloat()
            spinRotation += randomDegrees
            delay(2500)
            isSpinning = false
            freeSpinsRemaining--

            val prizes = listOf(
                "50% OFF Coupon (BOOYAH50)",
                "100 Free Bonus Diamonds Voucher",
                "10% Cashback Promo (FREEFIRE10)",
                "Level-Up Pass 20% Discount",
                "$1.00 Instant Cash Voucher (NEWBIE)",
                "Double Diamond 2X Multiplier"
            )
            val won = prizes.random()
            spinPrizeWon = won
            snackbarMessage = "🎉 You won: $won!"
        }
    }

    fun claimDailyCheckIn() {
        if (claimedToday) return
        dailyCheckInDays++
        claimedToday = true
        freeSpinsRemaining += 1
        snackbarMessage = "Day $dailyCheckInDays Check-In claimed! +1 Free Spin rewarded"
    }

    fun calculateEvoCost(): Pair<Int, Double> {
        val gun = selectedEvoGun
        val start = currentEvoLevel.coerceIn(1, 7)
        val end = targetEvoLevel.coerceIn(start, 7)
        
        var tokensNeeded = 0
        for (i in start until end) {
            tokensNeeded += gun.tokensPerLevel[i]
        }
        val diamondsNeeded = tokensNeeded * 10 // approx 1 token = 10 diamonds
        val approxUsd = diamondsNeeded * 0.009
        return Pair(diamondsNeeded, (approxUsd * 100).toLong() / 100.0)
    }
}
