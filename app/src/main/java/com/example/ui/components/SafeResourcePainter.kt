package com.example.ui.components

import android.util.Log
import android.util.TypedValue
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
import org.xmlpull.v1.XmlPullParser

@Composable
fun safePainterResource(
    @DrawableRes id: Int,
    fallbackIcon: ImageVector = Icons.Default.AccountBalanceWallet
): Painter {
    val context = LocalContext.current
    val isValid = remember(id, context) {
        if (id == 0) return@remember false
        try {
            val value = TypedValue()
            context.resources.getValue(id, value, true)
            val path = value.string?.toString() ?: ""
            if (path.endsWith(".xml", ignoreCase = true)) {
                val parser = context.resources.getXml(id)
                var type = parser.eventType
                while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                    type = parser.next()
                }
                if (type == XmlPullParser.START_TAG) {
                    val rootTagName = parser.name
                    rootTagName == "vector" || rootTagName == "animated-vector" ||
                            rootTagName == "layer-list" || rootTagName == "selector" ||
                            rootTagName == "shape" || rootTagName == "bitmap" ||
                            rootTagName == "adaptive-icon"
                } else {
                    false
                }
            } else {
                // Raster images (PNG, JPG, WEBP)
                true
            }
        } catch (e: Throwable) {
            Log.e("SafeResourcePainter", "Error inspecting resource $id: ${e.message}")
            false
        }
    }

    return if (isValid) {
        painterResource(id = id)
    } else {
        rememberVectorPainter(image = fallbackIcon)
    }
}
