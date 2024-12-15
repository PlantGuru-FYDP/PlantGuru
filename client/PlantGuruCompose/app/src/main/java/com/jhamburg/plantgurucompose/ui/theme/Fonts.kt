package com.jhamburg.plantgurucompose.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jhamburg.plantgurucompose.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val OjujuFont = FontFamily(
    Font(
        googleFont = GoogleFont("Ojuju"),
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Ojuju"),
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)

val RighteousFont = FontFamily(
    Font(
        googleFont = GoogleFont("Righteous"),
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

val RubikBubblesFont = FontFamily(
    Font(
        googleFont = GoogleFont("Rubik Bubbles"),
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

//val LogoFont = OjujuFont
val LogoFont = RighteousFont
// val LogoFont = RubikBubblesFont

val RalewayFont = FontFamily(
    Font(
        googleFont = GoogleFont("Raleway"),
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Raleway"),
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)

// val SpaceGroteskFont = FontFamily(
//     Font(
//         googleFont = GoogleFont("Space Grotesk"),
//         fontProvider = provider,
//         weight = FontWeight.Normal
//     ),
//     Font(
//         googleFont = GoogleFont("Space Grotesk"),
//         fontProvider = provider,
//         weight = FontWeight.Bold
//     )
// )
 