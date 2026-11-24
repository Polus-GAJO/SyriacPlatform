package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.syriacplatform.presentation.theme.LiturgicalTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShhimoQintoSelector(selectedQintoId: Long, onSelect: (Long) -> Unit) {
    val names = listOf("ܩܰܕܡܳܝܬܳܐ", "ܬܪܰܝܳܢܺܝܬܳܐ", "ܬܠܺܝܬܳܝܬܳܐ", "ܪܒܺܝܥܳܝܬܳܐ",
        "ܚܡܺܝܫܳܝܬܳܐ", "ܫܬܺܝܬܳܝܬܳܐ", "ܫܒܺܝܥܳܝܬܳܐ", "ܬܡܺܝܢܳܝܬܳܐ")
    var open by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterRight) {
        OutlinedButton(
            onClick = { open = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Text("ܩܺܝܢܬܳܐ ${names[(selectedQintoId - 1).toInt()]}", style = LiturgicalTextStyles.section().copy(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ))
            Spacer(Modifier.width(8.dp))
            Text("▾")
        }
    }
    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Text("ܩܺܝ̈ܢܳܬܳܐ", style = LiturgicalTextStyles.section(), modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
                names.forEachIndexed { index, name ->
                    val id = index.toLong() + 1L
                    TextButton(onClick = { onSelect(id); open = false }, modifier = Modifier.fillMaxWidth()) {
                        Text(name, style = LiturgicalTextStyles.section(), modifier = Modifier.weight(1f))
                        RadioButton(selected = selectedQintoId == id, onClick = null)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
