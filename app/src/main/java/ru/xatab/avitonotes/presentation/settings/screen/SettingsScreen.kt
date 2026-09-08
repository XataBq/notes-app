package ru.xatab.avitonotes.presentation.settings.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.ThemeMode
import ru.xatab.avitonotes.presentation.common.components.ErrorNotice
import ru.xatab.avitonotes.presentation.common.components.ErrorSnackbar
import ru.xatab.avitonotes.presentation.common.components.ScreenHeader
import ru.xatab.avitonotes.presentation.common.state.LoadState
import ru.xatab.avitonotes.presentation.settings.components.SettingSection
import ru.xatab.avitonotes.presentation.settings.viewmodel.SettingsViewModel
import ru.xatab.avitonotes.presentation.theme.color

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val resources = androidx.compose.ui.platform.LocalContext.current.resources
    val snack = remember { SnackbarHostState() }
    ErrorSnackbar(state.error, snack, vm::dismissError)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = { ScreenHeader(stringResource(R.string.tab_settings)) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.giga_chat),
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = vm::balance, enabled = state.balance != LoadState.Loading) {
                        Icon(Icons.Outlined.Refresh, stringResource(R.string.balance_refresh))
                    }
                }
                when (val balance = state.balance) {
                    LoadState.Loading -> Row(
                        Modifier.padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(23.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.balance_loading))
                    }
                    is LoadState.Error -> ErrorNotice(balance.message, vm::balance)
                    is LoadState.Ready -> {
                        if (balance.value.isEmpty()) {
                            Text(
                                stringResource(R.string.balance_empty),
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                        balance.value.forEach { entry ->
                            Text(
                                NumberFormat.getIntegerInstance(Locale.getDefault()).format(entry.value),
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                            Text(
                                stringResource(R.string.balance_package, entry.usage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            SettingSection {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(top = 12.dp)) {
                    ThemeMode.entries.forEach { theme ->
                        FilterChip(selected = state.settings.theme == theme, onClick = {
                            vm.theme(theme)
                        }, label = {
                            Text(
                                when (theme) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                },
                            )
                        })
                    }
                }
            }
            SettingSection {
                Text(stringResource(R.string.settings_accent), style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 16.dp),
                ) {
                    AccentColor.entries.forEach { accent ->
                        val selected = state.settings.accent == accent
                        val name = when (accent) {
                            AccentColor.BLUE -> stringResource(R.string.accent_blue)
                            AccentColor.PURPLE -> stringResource(R.string.accent_purple)
                            AccentColor.GREEN -> stringResource(R.string.accent_green)
                            AccentColor.CORAL -> stringResource(R.string.accent_coral)
                        }
                        Surface(
                            onClick = {
                                vm.accent(accent)
                            },
                            shape = CircleShape,
                            color = accent.color(),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription =
                                    if (selected) resources.getString(R.string.accent_selected, name) else name
                            },
                            border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                        ) {
                            Box(contentAlignment = Alignment.Center) { if (selected) Icon(Icons.Outlined.Check, null) }
                        }
                    }
                }
                Text(
                    stringResource(R.string.settings_accent_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_reset))
            }
        }
    }
}
