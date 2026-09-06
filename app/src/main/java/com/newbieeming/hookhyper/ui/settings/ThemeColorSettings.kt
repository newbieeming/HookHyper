package com.newbieeming.hookhyper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.R
import com.newbieeming.hookhyper.core.ui.theme.ThemeColor
import com.newbieeming.hookhyper.ui.component.AdaptiveCard

@Composable
fun ThemeColorSettings(selected: ThemeColor, onSelect: (ThemeColor) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    AdaptiveCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.theme_color_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.theme_color_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.width(156.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        stringResource(selected.labelRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ThemeColor.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes())) },
                            leadingIcon = {
                                if (option == selected) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            onClick = {
                                expanded = false
                                onSelect(option)
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun ThemeColor.labelRes(): Int = when (this) {
    ThemeColor.DYNAMIC -> R.string.theme_color_dynamic
    ThemeColor.DEFAULT -> R.string.theme_color_default
    ThemeColor.PINK -> R.string.theme_color_pink
    ThemeColor.RED -> R.string.theme_color_red
    ThemeColor.ORANGE -> R.string.theme_color_orange
    ThemeColor.AMBER -> R.string.theme_color_amber
    ThemeColor.YELLOW -> R.string.theme_color_yellow
    ThemeColor.LIME -> R.string.theme_color_lime
    ThemeColor.GREEN -> R.string.theme_color_green
    ThemeColor.CYAN -> R.string.theme_color_cyan
    ThemeColor.LIGHT_BLUE -> R.string.theme_color_light_blue
    ThemeColor.BLUE -> R.string.theme_color_blue
    ThemeColor.INDIGO -> R.string.theme_color_indigo
    ThemeColor.PURPLE -> R.string.theme_color_purple
    ThemeColor.DEEP_PURPLE -> R.string.theme_color_deep_purple
    ThemeColor.BLUE_GREY -> R.string.theme_color_blue_grey
    ThemeColor.BROWN -> R.string.theme_color_brown
}
