package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    search: (@Composable () -> Unit)? = null,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("screen-header")
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AvitoMark()
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (search == null) Modifier.weight(1f) else Modifier.widthIn(max = 180.dp),
            )
            if (search != null) Box(Modifier.weight(1f).padding(horizontal = 8.dp)) { search() }
            actions()
        }
    }
}
