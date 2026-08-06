package com.example.ui.components

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat

@Composable
fun safePainterResource(
    @DrawableRes id: Int,
    fallbackIcon: ImageVector = Icons.Default.AccountBalanceWallet
): Painter {
    val context = LocalContext.current
    val fallbackPainter = rememberVectorPainter(image = fallbackIcon)
    if (id == 0) return fallbackPainter

    val (bitmapPainter, isValidXml) = remember(id, context) {
        try {
            val drawable = ContextCompat.getDrawable(context, id)
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                Pair(BitmapPainter(drawable.bitmap.asImageBitmap()), false)
            } else if (drawable != null) {
                Pair(null, true)
            } else {
                Pair(null, false)
            }
        } catch (e: Throwable) {
            Log.w("SafeResourcePainter", "Error loading drawable $id: ${e.message}")
            Pair(null, false)
        }
    }

    return when {
        bitmapPainter != null -> bitmapPainter
        isValidXml -> painterResource(id = id)
        else -> fallbackPainter
    }
}
