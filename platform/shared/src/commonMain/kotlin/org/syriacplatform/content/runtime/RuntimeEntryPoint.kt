package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.EntryPoint

/**
 * EntryPoint جاهزة للبدء منها داخل Runtime.
 *
 * Schema v1 يدعم Occasion target فقط.
 */
data class RuntimeEntryPoint(
    val entryPoint: EntryPoint,
    val occasion: RuntimeOccasion
)