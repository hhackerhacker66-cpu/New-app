package com.example.data.model

enum class TopUpCategory(val title: String, val iconResName: String) {
    DIAMONDS("Diamonds", "diamond"),
    MEMBERSHIPS("Memberships", "card_membership"),
    SPECIAL_CRATES("Special Airdrops", "inventory_2"),
    LEVEL_UP_PASS("Level Up Pass", "trending_up")
}

enum class ServerRegion(val displayName: String, val code: String, val flag: String) {
    GLOBAL("Global Server", "GLB", "🌐"),
    INDIA("India (IND)", "IND", "🇮🇳"),
    INDONESIA("Indonesia (ID)", "ID", "🇮🇩"),
    BRAZIL("Brazil (BR)", "BR", "🇧🇷"),
    MENA("Middle East (MENA)", "MENA", "🇦🇪"),
    EUROPE("Europe (EU)", "EU", "🇪🇺"),
    SINGAPORE("Singapore / SEA", "SG", "🇸🇬"),
    LATIN_AMERICA("Latin America (LATAM)", "LATAM", "🇲🇽"),
    NORTH_AMERICA("North America (NA)", "NA", "🇺🇸")
}

data class TopUpItem(
    val id: String,
    val category: TopUpCategory,
    val name: String,
    val diamondCount: Int,
    val bonusDiamonds: Int = 0,
    val originalPrice: Double,
    val price: Double,
    val badge: String? = null,
    val description: String = "",
    val isPopular: Boolean = false,
    val isDoubleBonus: Boolean = false
)

enum class PaymentType(
    val displayName: String,
    val iconName: String,
    val processingFee: Double = 0.0,
    val subtitle: String = "0% Fee Capped"
) {
    QR_SCAN_PAY("Instant QR Code Scan & Pay", "qr_code_scanner", 0.0, "Auto-Verified • 0% Fee"),
    UPI_GPAY("Google Pay / UPI FastPay", "payments", 0.0, "Zero Gateway Fee"),
    PHONEPE("PhonePe / Paytm Wallet", "account_balance_wallet", 0.0, "Zero Gateway Fee"),
    CREDIT_CARD("Credit / Debit Card (Visa/MC)", "credit_card", 0.0, "Zero Surcharge"),
    RAZER_GOLD("Razer Gold Direct Pin", "stars", 0.0, "Official Partner • 0% Fee"),
    UNIPIN("UniPin Credits / Wallet", "account_balance", 0.0, "Instant Delivery"),
    APPLE_PAY("Apple Pay / Touch ID", "apple", 0.0, "Zero Fee Capped"),
    PAYPAL("PayPal Global Checkout", "paypal", 0.0, "Zero Fee Promotional Cap")
}

data class PromoCode(
    val code: String,
    val discountPercent: Int = 0,
    val flatDiscount: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val description: String
)

data class VerifiedPlayer(
    val playerId: String,
    val nickname: String,
    val level: Int,
    val rank: String,
    val region: ServerRegion,
    val avatarBadge: String = "👑"
)
