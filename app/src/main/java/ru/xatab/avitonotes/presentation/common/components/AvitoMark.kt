package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AvitoMark(modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        val radius = size.width * 0.19f
        drawCircle(Color(0xFF00AAFF), radius, Offset(size.width * 0.25f, size.height * 0.25f))
        drawCircle(Color(0xFF965EEB), radius, Offset(size.width * 0.75f, size.height * 0.25f))
        drawCircle(Color(0xFFA0D934), radius, Offset(size.width * 0.25f, size.height * 0.75f))
        drawCircle(Color(0xFFFF6163), radius, Offset(size.width * 0.75f, size.height * 0.75f))
    }
}
