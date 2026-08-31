package com.example.data.repository

import com.example.data.local.OrderEntity
import com.example.data.local.SavedAccountEntity
import com.example.data.local.TopUpDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TopUpRepository(private val dao: TopUpDao) {

    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allSavedAccounts: Flow<List<SavedAccountEntity>> = dao.getAllSavedAccounts()

    suspend fun saveOrder(order: OrderEntity): Long = dao.insertOrder(order)
    suspend fun deleteOrder(id: Long) = dao.deleteOrderById(id)
    suspend fun clearOrders() = dao.clearAllOrders()

    suspend fun saveAccount(account: SavedAccountEntity): Long = dao.insertSavedAccount(account)
    suspend fun updateAccount(account: SavedAccountEntity) = dao.updateSavedAccount(account)
    suspend fun deleteAccount(account: SavedAccountEntity) = dao.deleteSavedAccount(account)

    suspend fun initDefaultAccountsIfEmpty() {
        val sampleAccounts = listOf(
            SavedAccountEntity(
                playerId = "2849104829",
                nickname = "⚡SK_Vampire⚡",
                serverRegion = "GLB",
                rankBadge = "Grandmaster",
                level = 72,
                isFavorite = true
            ),
            SavedAccountEntity(
                playerId = "1928475610",
                nickname = "🔥RAISTAR_99🔥",
                serverRegion = "IND",
                rankBadge = "Heroic V",
                level = 68,
                isFavorite = false
            ),
            SavedAccountEntity(
                playerId = "3847291045",
                nickname = "亗TOXIC_KING亗",
                serverRegion = "ID",
                rankBadge = "Master",
                level = 61,
                isFavorite = false
            )
        )
        for (acc in sampleAccounts) {
            val existing = dao.getAccountByPlayerId(acc.playerId)
            if (existing == null) {
                dao.insertSavedAccount(acc)
            }
        }
    }

    fun getCatalog(): List<TopUpItem> = listOf(
        // Direct Diamonds
        TopUpItem(
            id = "d_50",
            category = TopUpCategory.DIAMONDS,
            name = "50 Diamonds",
            diamondCount = 50,
            bonusDiamonds = 5,
            originalPrice = 0.60,
            price = 0.49,
            description = "Starter diamond pack"
        ),
        TopUpItem(
            id = "d_100",
            category = TopUpCategory.DIAMONDS,
            name = "100 Diamonds",
            diamondCount = 100,
            bonusDiamonds = 100, // 2x double bonus
            originalPrice = 1.99,
            price = 0.99,
            badge = "2X FIRST TOP UP",
            isDoubleBonus = true,
            isPopular = true,
            description = "Double Diamonds for 1st recharge"
        ),
        TopUpItem(
            id = "d_310",
            category = TopUpCategory.DIAMONDS,
            name = "310 Diamonds",
            diamondCount = 310,
            bonusDiamonds = 31,
            originalPrice = 3.49,
            price = 2.99,
            badge = "+10% BONUS",
            description = "Includes 31 Extra Diamonds"
        ),
        TopUpItem(
            id = "d_520",
            category = TopUpCategory.DIAMONDS,
            name = "520 Diamonds",
            diamondCount = 520,
            bonusDiamonds = 52,
            originalPrice = 5.99,
            price = 4.99,
            badge = "BEST VALUE",
            isPopular = true,
            description = "Popular for Booyah Pass & Events"
        ),
        TopUpItem(
            id = "d_1060",
            category = TopUpCategory.DIAMONDS,
            name = "1060 Diamonds",
            diamondCount = 1060,
            bonusDiamonds = 106,
            originalPrice = 11.99,
            price = 9.99,
            badge = "+106 FREE",
            description = "Unlock Incubator and Lucky Royale"
        ),
        TopUpItem(
            id = "d_2180",
            category = TopUpCategory.DIAMONDS,
            name = "2180 Diamonds",
            diamondCount = 2180,
            bonusDiamonds = 218,
            originalPrice = 23.99,
            price = 19.99,
            badge = "PRO PACK",
            description = "For Evo Gun upgrades & Emotes"
        ),
        TopUpItem(
            id = "d_5600",
            category = TopUpCategory.DIAMONDS,
            name = "5600 Diamonds",
            diamondCount = 5600,
            bonusDiamonds = 560,
            originalPrice = 59.99,
            price = 49.99,
            badge = "MEGA VAULT",
            description = "Maximum bonus value pack"
        ),

        // Memberships & Passes
        TopUpItem(
            id = "m_weekly",
            category = TopUpCategory.MEMBERSHIPS,
            name = "Weekly Membership",
            diamondCount = 450,
            bonusDiamonds = 0,
            originalPrice = 2.49,
            price = 1.99,
            badge = "DAILY 💎",
            isPopular = true,
            description = "100 Instant 💎 + 50 💎 Daily (7 days) + Special Icon"
        ),
        TopUpItem(
            id = "m_monthly",
            category = TopUpCategory.MEMBERSHIPS,
            name = "Monthly Membership",
            diamondCount = 2600,
            bonusDiamonds = 0,
            originalPrice = 12.99,
            price = 9.99,
            badge = "500% VALUE",
            isPopular = true,
            description = "500 Instant 💎 + 70 💎 Daily (30 days) + Universal EP Badge"
        ),
        TopUpItem(
            id = "m_weekly_lite",
            category = TopUpCategory.MEMBERSHIPS,
            name = "Weekly Lite Pass",
            diamondCount = 180,
            bonusDiamonds = 0,
            originalPrice = 1.20,
            price = 0.89,
            badge = "BUDGET",
            description = "30 Instant 💎 + 25 💎 Daily for 6 days"
        ),
        TopUpItem(
            id = "m_booyah_pass",
            category = TopUpCategory.MEMBERSHIPS,
            name = "Booyah Pass Premium",
            diamondCount = 0,
            bonusDiamonds = 0,
            originalPrice = 5.99,
            price = 4.49,
            badge = "SEASON PASS",
            isPopular = true,
            description = "Unlock 100 Tiers, Mythic Skin & Emote instantly"
        ),
        TopUpItem(
            id = "m_booyah_pass_plus",
            category = TopUpCategory.MEMBERSHIPS,
            name = "Booyah Pass Premium Plus",
            diamondCount = 0,
            bonusDiamonds = 0,
            originalPrice = 14.99,
            price = 10.99,
            badge = "50 TIERS BOOST",
            description = "50 Tiers unlock + Exclusive Avatar & Border"
        ),

        // Special Airdrops & Crates
        TopUpItem(
            id = "c_airdrop_1",
            category = TopUpCategory.SPECIAL_CRATES,
            name = "Super Airdrop (100💎 + AK47 Blue Flame)",
            diamondCount = 100,
            bonusDiamonds = 0,
            originalPrice = 1.99,
            price = 1.29,
            badge = "LIMITED TIME",
            isPopular = true,
            description = "100 Instant Diamonds + 5x AK47 Draco Crates"
        ),
        TopUpItem(
            id = "c_airdrop_2",
            category = TopUpCategory.SPECIAL_CRATES,
            name = "Emote Party Crate (300💎 + Tea Time)",
            diamondCount = 300,
            bonusDiamonds = 0,
            originalPrice = 3.99,
            price = 2.49,
            badge = "HOT",
            description = "300 Diamonds + Legendary Emote Choice Box"
        ),
        TopUpItem(
            id = "c_evo_tokens",
            category = TopUpCategory.SPECIAL_CRATES,
            name = "Evo Gun Token Chest (500💎 + 100 Tokens)",
            diamondCount = 500,
            bonusDiamonds = 50,
            originalPrice = 7.99,
            price = 5.99,
            badge = "EVO UPGRADE",
            description = "550 Diamonds + 100 Universal Evo Tokens"
        ),
        TopUpItem(
            id = "c_hyperbook",
            category = TopUpCategory.SPECIAL_CRATES,
            name = "Hyperbook Page Vault (1000💎 + 20 Pages)",
            diamondCount = 1000,
            bonusDiamonds = 100,
            originalPrice = 11.99,
            price = 8.99,
            badge = "RARE",
            description = "1100 Diamonds + Hyperbook Page tokens"
        ),

        // Level Up Pass
        TopUpItem(
            id = "l_pass_800",
            category = TopUpCategory.LEVEL_UP_PASS,
            name = "Level-Up Pass (802 Diamonds)",
            diamondCount = 802,
            bonusDiamonds = 0,
            originalPrice = 5.99,
            price = 3.99,
            badge = "4.2X RETURN",
            isPopular = true,
            description = "Claim 802 Diamonds progressively as player level rises (Lv 1 - 30)"
        )
    )

    fun getPromoCodes(): List<PromoCode> = listOf(
        PromoCode("BOOYAH50", discountPercent = 50, minOrderAmount = 2.0, description = "50% OFF (Max $5 discount)"),
        PromoCode("FREEFIRE10", discountPercent = 10, minOrderAmount = 0.5, description = "10% OFF on all packs"),
        PromoCode("DIAMOND20", discountPercent = 20, minOrderAmount = 4.0, description = "20% OFF on orders over $4.00"),
        PromoCode("NEWBIE", flatDiscount = 1.0, minOrderAmount = 2.0, description = "$1.00 OFF on orders above $2.00")
    )

    fun verifyPlayer(playerId: String, region: ServerRegion): VerifiedPlayer {
        val cleanId = playerId.trim()
        val nickVariants = listOf(
            "⚡SK_Vampire⚡",
            "🔥RAISTAR_99🔥",
            "亗TOXIC_KING亗",
            "★SlayeR★",
            "〆GHOST_FF〆",
            "⚡NOBRU_GOD⚡",
            "✿QUEEN_GAMER✿",
            "꧁༺NINJA༻꧂"
        )
        val ranks = listOf("Grandmaster", "Heroic V", "Master III", "Diamond IV", "Elite Heroic")
        val index = (cleanId.hashCode().toLong() and 0x7FFFFFFF).toInt() % nickVariants.size
        val level = 45 + (cleanId.hashCode().toLong() and 0x7FFFFFFF % 40).toInt() // 45 to 84

        return VerifiedPlayer(
            playerId = cleanId,
            nickname = nickVariants[index],
            level = level,
            rank = ranks[index % ranks.size],
            region = region,
            avatarBadge = if (level > 65) "👑" else "🎖️"
        )
    }
}
