package com.example.model

import androidx.compose.ui.graphics.Color

enum class CharacterType(
    val id: String,
    val displayName: String,
    val kanji: String,
    val japaneseVoiceActor: String,
    val signatureMove: String,
    val suitColor: Color,
    val accentColor: Color,
    val hairStyle: HairStyle
) {
    GOKU(
        id = "goku",
        displayName = "Goku",
        kanji = "孫悟空",
        japaneseVoiceActor = "野沢雅子 (Masako Nozawa)",
        signatureMove = "Kamehameha",
        suitColor = Color(0xFFFF6D00), // Orange Gi
        accentColor = Color(0xFF1565C0), // Blue undershirt & belt
        hairStyle = HairStyle.GOKU_SPIKES
    ),
    VEGETA(
        id = "vegeta",
        displayName = "Vegeta",
        kanji = "ベジータ",
        japaneseVoiceActor = "堀川りょう (Ryō Horikawa)",
        signatureMove = "Final Flash",
        suitColor = Color(0xFF0D47A1), // Blue bodysuit
        accentColor = Color(0xFFFFFFFF), // White combat armor vest with gold straps
        hairStyle = HairStyle.VEGETA_FLAME
    )
}

enum class HairStyle {
    GOKU_SPIKES,
    VEGETA_FLAME
}

enum class SaiyanForm(
    val formId: String,
    val displayName: String,
    val japaneseName: String,
    val requiredScore: Int,
    val primaryHairColor: Color,
    val secondaryHairColor: Color,
    val auraInnerColor: Color,
    val auraOuterColor: Color,
    val hasLightning: Boolean,
    val isGodly: Boolean,
    val isOozaru: Boolean,
    val powerMultiplier: String,
    val voiceQuoteGoku: String,
    val voiceQuoteVegeta: String
) {
    BASE(
        formId = "base",
        displayName = "Base Form",
        japaneseName = "通常形態",
        requiredScore = 0,
        primaryHairColor = Color(0xFF1A1A1A),
        secondaryHairColor = Color(0xFF333333),
        auraInnerColor = Color(0x33FFFFFF),
        auraOuterColor = Color(0x11FFFFFF),
        hasLightning = false,
        isGodly = false,
        isOozaru = false,
        powerMultiplier = "戦闘力: 9,001",
        voiceQuoteGoku = "いくぞ！絶対に負けないぞ！ (Ikuzo! Zettai ni makenai zo!)",
        voiceQuoteVegeta = "ふん！俺の邪魔をするな！ (Fun! Ore no jama o suru na!)"
    ),
    SUPER_SAIYAN(
        formId = "ssj1",
        displayName = "Super Saiyan",
        japaneseName = "超サイヤ人",
        requiredScore = 5,
        primaryHairColor = Color(0xFFFFEA00),
        secondaryHairColor = Color(0xFFFFD600),
        auraInnerColor = Color(0xAAFFEE58),
        auraOuterColor = Color(0x55FFC107),
        hasLightning = false,
        isGodly = false,
        isOozaru = false,
        powerMultiplier = "50倍 パワーブースト",
        voiceQuoteGoku = "許せんぞフリーザ！これが超サイヤ人だ！ (Yurusenai zo! Kore ga Sūpā Saiyajin da!)",
        voiceQuoteVegeta = "カカロットだけではない！俺も超サイヤ人だ！ (Ore mo Sūpā Saiyajin da!)"
    ),
    SUPER_SAIYAN_2(
        formId = "ssj2",
        displayName = "Super Saiyan 2",
        japaneseName = "超サイヤ人2",
        requiredScore = 10,
        primaryHairColor = Color(0xFFFFF59D),
        secondaryHairColor = Color(0xFFFFEE58),
        auraInnerColor = Color(0xCCFFF176),
        auraOuterColor = Color(0x77FFD54F),
        hasLightning = true,
        isGodly = false,
        isOozaru = false,
        powerMultiplier = "100倍 超覚醒",
        voiceQuoteGoku = "まだだ！超サイヤ人2だ！波ーー！ (Mada da! Sūpā Saiyajin Tsū da!)",
        voiceQuoteVegeta = "俺はサイヤ人の王子ベジータ様だ！ (Ore wa Saiyajin no ōji Bejīta-sama da!)"
    ),
    SUPER_SAIYAN_3(
        formId = "ssj3",
        displayName = "Super Saiyan 3",
        japaneseName = "超サイヤ人3",
        requiredScore = 20,
        primaryHairColor = Color(0xFFFFD54F),
        secondaryHairColor = Color(0xFFFFB300),
        auraInnerColor = Color(0xDDFFCA28),
        auraOuterColor = Color(0x88FF8F00),
        hasLightning = true,
        isGodly = false,
        isOozaru = false,
        powerMultiplier = "400倍 限界突破",
        voiceQuoteGoku = "そしてこれが…超サイヤ人3だ！うおおお！ (Soshite kore ga... Sūpā Saiyajin Surī da!)",
        voiceQuoteVegeta = "カカロットにできて俺にできないわけがない！超サイヤ人3！ (Sūpā Saiyajin Surī!)"
    ),
    SUPER_SAIYAN_GOD(
        formId = "ssg",
        displayName = "Super Saiyan God",
        japaneseName = "超サイヤ人ゴッド",
        requiredScore = 25,
        primaryHairColor = Color(0xFFE91E63),
        secondaryHairColor = Color(0xFFFF4081),
        auraInnerColor = Color(0xCCFF1744),
        auraOuterColor = Color(0x66FF5252),
        hasLightning = false,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "神の気 (神聖領域)",
        voiceQuoteGoku = "これが超サイヤ人ゴッドか！すげえ気だ！ (Kore ga Sūpā Saiyajin Goddo ka!)",
        voiceQuoteVegeta = "神の領域だと？くだらん！俺の力を見ろ！ (Kami no ryōiki dato? Ore no chikara o miro!)"
    ),
    SUPER_SAIYAN_BLUE(
        formId = "ssb",
        displayName = "Super Saiyan Blue",
        japaneseName = "超サイヤ人ブルー",
        requiredScore = 35,
        primaryHairColor = Color(0xFF00E5FF),
        secondaryHairColor = Color(0xFF00B0FF),
        auraInnerColor = Color(0xDD00E5FF),
        auraOuterColor = Color(0x662979FF),
        hasLightning = true,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "超サイヤ人ゴッドSS",
        voiceQuoteGoku = "超サイヤ人ブルー！界王拳！波ーー！ (Sūpā Saiyajin Burū! Kaiō-ken!)",
        voiceQuoteVegeta = "これが新生ベジータ様の姿だ！超サイヤ人ブルー！ (Shinsei Bejīta-sama no Sugata da!)"
    ),
    ULTRA_INSTINCT(
        formId = "ui",
        displayName = "Ultra Instinct -Sign-",
        japaneseName = "身勝手の極意”兆”",
        requiredScore = 40,
        primaryHairColor = Color(0xFF263238),
        secondaryHairColor = Color(0xFF90A4AE),
        auraInnerColor = Color(0xCCB0BEC5),
        auraOuterColor = Color(0x55ECEFF1),
        hasLightning = false,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "神の御技 (兆)",
        voiceQuoteGoku = "身勝手の極意…体が勝手に動く！ (Migatte no Gokui... Karada ga katte ni ugoku!)",
        voiceQuoteVegeta = ""
    ),
    ULTRA_EGO(
        formId = "ue",
        displayName = "Ultra Ego",
        japaneseName = "我儘の極意",
        requiredScore = 40,
        primaryHairColor = Color(0xFF7B1FA2),
        secondaryHairColor = Color(0xFF9C27B0),
        auraInnerColor = Color(0xCC6A1B9A),
        auraOuterColor = Color(0x664A148C),
        hasLightning = false,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "破壊神の力 (破壊)",
        voiceQuoteGoku = "",
        voiceQuoteVegeta = "我が身を燃やせ！これぞ我儘の極意！破壊！ (Waga mi o moyase! Waga Tei no Gokui! Hakai!)"
    ),
    MASTERED_ULTRA_INSTINCT(
        formId = "mui",
        displayName = "Mastered Ultra Instinct",
        japaneseName = "身勝手の極意 (極)",
        requiredScore = 45,
        primaryHairColor = Color(0xFFFFFFFF),
        secondaryHairColor = Color(0xFFCFD8DC),
        auraInnerColor = Color(0xEEFFFFFF),
        auraOuterColor = Color(0x77B0BEC5),
        hasLightning = true,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "極限の境地 (完全体)",
        voiceQuoteGoku = "これが…身勝手の極意の完成形だ！波ーー！ (Kore ga Migatte no Gokui no kanseikei da!)",
        voiceQuoteVegeta = ""
    ),
    MASTERED_ULTRA_EGO(
        formId = "mue",
        displayName = "Mastered Ultra Ego",
        japaneseName = "我儘の極意 (極)",
        requiredScore = 45,
        primaryHairColor = Color(0xFFBA55D3),
        secondaryHairColor = Color(0xFFE040FB),
        auraInnerColor = Color(0xEE7B1FA2),
        auraOuterColor = Color(0x884A148C),
        hasLightning = true,
        isGodly = true,
        isOozaru = false,
        powerMultiplier = "真・破壊神",
        voiceQuoteGoku = "",
        voiceQuoteVegeta = "これこそがサイヤ人の究極の力だ！完全破壊！ (Kore koso ga Saiyajin no kyūkyoku no chikara da!)"
    ),
    // 5 levels after MUI/MUE: Black Oozaru
    BLACK_OOZARU(
        formId = "black_oozaru",
        displayName = "Black Oozaru",
        japaneseName = "黒き大猿 (Great Ape)",
        requiredScore = 50,
        primaryHairColor = Color(0xFF1B1B1B), // Pitch black beast fur
        secondaryHairColor = Color(0xFF000000),
        auraInnerColor = Color(0xCCB71C1C), // Beast crimson red
        auraOuterColor = Color(0x77212121),
        hasLightning = true,
        isGodly = false,
        isOozaru = true, // Bigger size & slower game speed!
        powerMultiplier = "巨獣大猿化 (10倍力)",
        voiceQuoteGoku = "グオオオオ！大猿の血が騒ぐ！ (GWOOOOH! Ōzaru no chi ga sawagu!)",
        voiceQuoteVegeta = "これぞサイヤ人の原点！黒き大猿だ！ウオオオ！ (Kore zo Saiyajin no genten! Kuroki Ōzaru da!)"
    ),
    // 5 more levels: Golden Oozaru
    GOLDEN_OOZARU(
        formId = "golden_oozaru",
        displayName = "Golden Oozaru",
        japaneseName = "黄金大猿 (Golden Great Ape)",
        requiredScore = 55,
        primaryHairColor = Color(0xFFFFD700), // Blazing golden fur
        secondaryHairColor = Color(0xFFFF8F00),
        auraInnerColor = Color(0xEEFFEA00),
        auraOuterColor = Color(0x88FF6D00),
        hasLightning = true,
        isGodly = false,
        isOozaru = true, // Bigger size & slower game speed!
        powerMultiplier = "超サイヤ大猿 (極限野生)",
        voiceQuoteGoku = "黄金の大猿だ！心を取り戻せ…！ウオオオ！ (Ōgon no Ōzaru da! Kokoro o torimodose!)",
        voiceQuoteVegeta = "フハハハ！黄金の大猿の力を制御してみせる！ (Ōgon no Ōzaru no chikara o seigyo shite miseru!)"
    ),
    // 10 levels later: Super Saiyan 4 (normal size returns!)
    SUPER_SAIYAN_4(
        formId = "ssj4",
        displayName = "Super Saiyan 4",
        japaneseName = "超サイヤ人4",
        requiredScore = 65,
        primaryHairColor = Color(0xFF212121), // Wild dark mane
        secondaryHairColor = Color(0xFFD50000), // Crimson body fur
        auraInnerColor = Color(0xEEFF1744),
        auraOuterColor = Color(0x88FFD600),
        hasLightning = true,
        isGodly = false,
        isOozaru = false, // Reverts to normal size & normal speed!
        powerMultiplier = "4,000倍 究極超戦士",
        voiceQuoteGoku = "これが…超サイヤ人4だ！10倍かめはめ波ーー！ (Kore ga... Sūpā Saiyajin Fō da!)",
        voiceQuoteVegeta = "カカロットに遅れをとるつもりはない！超サイヤ人4！ (Sūpā Saiyajin Fō!)"
    ),
    // 5 levels later: White Oozaru
    WHITE_OOZARU(
        formId = "white_oozaru",
        displayName = "White Oozaru",
        japaneseName = "白銀大猿 (Divine White Ape)",
        requiredScore = 70,
        primaryHairColor = Color(0xFFFFFFFF), // Pure white silver fur
        secondaryHairColor = Color(0xFFECEFF1),
        auraInnerColor = Color(0xEEEDE7F6),
        auraOuterColor = Color(0x777E57C2), // Divine astral aura
        hasLightning = true,
        isGodly = true,
        isOozaru = true, // Bigger size & slower game speed!
        powerMultiplier = "神話の白銀神獣",
        voiceQuoteGoku = "白銀の大猿…神をも超えた野生の咆哮だ！ (Hakugin no Ōzaru... Kami o koeta yasei da!)",
        voiceQuoteVegeta = "白銀大猿の高貴なる咆哮を聞け！うおおお！ (Hakugin Ōzaru no kōki naru hōkō o kike!)"
    ),
    // 10 levels later: White-colored Ultra Xeno Goku and Xeno Vegeta (normal size returns!)
    ULTRA_XENO(
        formId = "ultra_xeno",
        displayName = "Ultra Xeno Form",
        japaneseName = "超ゼノ形態 (Ultra Xeno)",
        requiredScore = 80,
        primaryHairColor = Color(0xFFFFFFFF), // Radiant divine white hair & robes
        secondaryHairColor = Color(0xFFCFD8DC),
        auraInnerColor = Color(0xFFFFFFFF),
        auraOuterColor = Color(0xAA00E5FF),
        hasLightning = true,
        isGodly = true,
        isOozaru = false, // Reverts to normal size & normal speed!
        powerMultiplier = "時空超越・白銀神極",
        voiceQuoteGoku = "時空を超えた俺たちの姿…ウルトラゼノ悟空だ！波ーー！ (Urutora Zeno Gokū da!)",
        voiceQuoteVegeta = "時空の果てまで轟け！ウルトラゼノベジータ様だ！ (Urutora Zeno Bejīta-sama da!)"
    );

    companion object {
        fun getFormForScore(score: Int, character: CharacterType): SaiyanForm {
            return when {
                score >= 80 -> ULTRA_XENO
                score >= 70 -> WHITE_OOZARU
                score >= 65 -> SUPER_SAIYAN_4
                score >= 55 -> GOLDEN_OOZARU
                score >= 50 -> BLACK_OOZARU
                score >= 45 -> {
                    if (character == CharacterType.GOKU) MASTERED_ULTRA_INSTINCT else MASTERED_ULTRA_EGO
                }
                score >= 40 -> {
                    if (character == CharacterType.GOKU) ULTRA_INSTINCT else ULTRA_EGO
                }
                score >= 35 -> SUPER_SAIYAN_BLUE
                score >= 25 -> SUPER_SAIYAN_GOD
                score >= 20 -> SUPER_SAIYAN_3
                score >= 10 -> SUPER_SAIYAN_2
                score >= 5 -> SUPER_SAIYAN
                else -> BASE
            }
        }
    }
}
