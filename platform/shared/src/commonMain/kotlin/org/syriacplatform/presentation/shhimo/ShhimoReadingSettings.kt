package org.syriacplatform.presentation.shhimo

import org.syriacplatform.presentation.theme.LocalizedControlText

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import org.syriacplatform.presentation.theme.LiturgicalFontFamilies
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.settings_font_size
import org.syriacplatform.resources.settings_font_size_value
import org.syriacplatform.resources.settings_interface_language
import org.syriacplatform.resources.settings_save
import org.syriacplatform.user.models.UserPreferences

/** Same user preference model, language options and font scale as the Occasion app. */
@Composable
fun ShhimoReadingSettings(
    preferences: UserPreferences,
    onSave: (UserPreferences) -> Unit
) {
    var languageOpen by remember { mutableStateOf(false) }
    var fontOpen by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { languageOpen = true }) {
            LocalizedControlText(stringResource(Res.string.settings_interface_language))
        }
        TextButton(onClick = { fontOpen = true }) {
            LocalizedControlText(stringResource(Res.string.settings_font_size))
        }
    }
    if (languageOpen) {
        Dialog(onDismissRequest = { languageOpen = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(24.dp)) {
                    LocalizedControlText(stringResource(Res.string.settings_interface_language))
                    listOf("ar" to "العربية", "en" to "English", "fr" to "Français", "de" to "Deutsch").forEach { (code, label) ->
                        TextButton(onClick = {
                            onSave(preferences.copy(interfaceLanguage = code))
                            languageOpen = false
                        }) {
                            Text((if (preferences.interfaceLanguage == code) "✓ " else "") + label, fontFamily = if (code == "ar") LiturgicalFontFamilies.arabic() else LiturgicalFontFamilies.latin())
                        }
                    }
                }
            }
        }
    }
    if (fontOpen) {
        var draftScale by remember(preferences.fontScale) { mutableStateOf(preferences.fontScale) }
        Dialog(onDismissRequest = { fontOpen = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(24.dp)) {
                    LocalizedControlText(stringResource(Res.string.settings_font_size_value, (draftScale * 100).toInt()))
                    Slider(
                        value = draftScale,
                        onValueChange = { draftScale = it },
                        valueRange = 0.8f..1.6f,
                        steps = 7
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { draftScale = 1f }) { Text("100%") }
                        TextButton(onClick = {
                            onSave(preferences.copy(fontScale = draftScale))
                            fontOpen = false
                        }) { LocalizedControlText(stringResource(Res.string.settings_save)) }
                    }
                }
            }
        }
    }
}
