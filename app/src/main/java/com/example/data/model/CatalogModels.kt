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

enum class PaymentType(val displayName: String, val iconName: String, val processingFee: Double = 0.0) {
    UPI_GPAY("Google Pay / UPI", "payments", 0.0),
    PHONEPE("PhonePe / Paytm", "account_balance_wallet", 0.0),
    CREDIT_CARD("Credit / Debit Card", "credit_card", 0.0),
    PAYPAL("PayPal", "paypal", 0.15),
    RAZER_GOLD("Razer Gold", "stars", 0.0),
    UNIPIN("UniPin Wallet", "account_balance", 0.0),
    APPLE_PAY("Apple Pay", "apple", 0.0)
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
