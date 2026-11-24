package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShhimoQintoSelector(selectedQintoId: Long, onSelect: (Long) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        listOf("ܐ", "ܒ", "ܓ", "ܕ", "ܗ", "ܘ", "ܙ", "ܚ").forEachIndexed { index, label ->
            val id = index.toLong() + 1L
            TextButton(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp),
                onClick = { onSelect(id) }
            ) {
                Text(if (selectedQintoId == id) "[$label]" else label)
            }
        }
    }
}
