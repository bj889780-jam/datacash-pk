package com.example.ui.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun safePainterResource(
    @DrawableRes id: Int,
    fallbackIcon: ImageVector = Icons.Default.AccountBalanceWallet
): Painter {
    val context = LocalContext.current
    val isValid = remember(id, context) {
        try {
            context.resources.getResourceName(id)
            true
        } catch (e: Throwable) {
            Log.e("SafeResourcePainter", "Resource $id invalid: ${e.message}")
            false
        }
    }
    return if (isValid) {
        painterResource(id = id)
    } else {
        rememberVectorPainter(image = fallbackIcon)
    }
}
