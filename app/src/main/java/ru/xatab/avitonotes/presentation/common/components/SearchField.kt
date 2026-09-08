package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.input.keyboardDismissTarget

@Composable
fun SearchField(
    query: String,
    onQuery: (String) -> Unit,
    onSearch: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        onSearch()
        focus.clearFocus()
        keyboard?.hide()
        Unit
    }
    BasicTextField(
        value = query,
        onValueChange = onQuery,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("search-field")
            .keyboardDismissTarget()
            .semantics { contentDescription = label },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { submit() }),
        decorationBox = { field ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    field()
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQuery("") }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Outlined.Close,
                            stringResource(R.string.search_clear),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                IconButton(onClick = submit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Search, stringResource(R.string.search_submit), modifier = Modifier.size(20.dp))
                }
            }
        },
    )
}
