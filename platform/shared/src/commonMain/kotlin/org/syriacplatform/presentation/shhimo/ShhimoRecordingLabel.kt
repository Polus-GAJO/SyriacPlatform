package org.syriacplatform.presentation.shhimo

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.shhimo_performance_recording

/** Expose a localized label without leaking internal generated resources across modules. */
@Composable
fun shhimoRecordingLabel(): String =
    stringResource(Res.string.shhimo_performance_recording)
