package com.learncodes.mynote.ui.theme

data class CustomColors(
    val primary: Int,
    val accent: Int,
    val background: Int
)

enum class AppTheme(
    val themeName: String,
    val primaryColor: Int,
    val accentColor: Int,
    val backgroundColor: Int
) {
    DEFAULT(
        "Default",
        0xFF6200EE.toInt(),
        0xFF03DAC5.toInt(),
        0xFFF5F5F5.toInt()
    ),
    OCEAN(
        "Ocean Blue",
        0xFF006064.toInt(),
        0xFF00BCD4.toInt(),
        0xFFE0F7FA.toInt()
    ),
    FOREST(
        "Forest Green",
        0xFF2E7D32.toInt(),
        0xFF66BB6A.toInt(),
        0xFFE8F5E9.toInt()
    ),
    SUNSET(
        "Sunset Orange",
        0xFFE65100.toInt(),
        0xFFFF9800.toInt(),
        0xFFFFF3E0.toInt()
    ),
    LAVENDER(
        "Lavender",
        0xFF7B1FA2.toInt(),
        0xFFBA68C8.toInt(),
        0xFFF3E5F5.toInt()
    ),
    DARK(
        "Dark Mode",
        0xFF1976D2.toInt(),
        0xFF64B5F6.toInt(),
        0xFF121212.toInt()
    ),
    ROSE(
        "Rose Pink",
        0xFFC2185B.toInt(),
        0xFFF06292.toInt(),
        0xFFFCE4EC.toInt()
    )
}